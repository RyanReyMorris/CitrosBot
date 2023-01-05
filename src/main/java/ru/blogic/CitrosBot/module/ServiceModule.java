package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.ServiceCallRequest;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.ServiceCallRequestService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Модуль чат-бота, отвечающий за техподдержку пользователей
 *
 * @author eyakimov
 */
@Service
public class ServiceModule implements Module {
    /**
     * Мапа, хранящая в себе единственный запрос для конкретного админа. Ключ - id админа, значение - id запроса в техподдержку.
     * Мапа необходима для быстрого ответа админа пользователю по его вопросу.
     * После ответа админа, запись с его id удаляется из мапы.
     */
    private final Map<Long, Long> mapOfServiceRequest = new HashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceCallRequestService serviceCallRequestService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private MessageService messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        UserEntity userEntity = userService.findUserById(chatId);
        if (userEntity.isAdmin()) {
            if (mapOfServiceRequest.get(chatId) != null) {
                ServiceCallRequest serviceCallRequest = serviceCallRequestService.findServiceCallRequestById(mapOfServiceRequest.get(chatId)).get();
                UserEntity userToReply = userService.findUserById(serviceCallRequest.getFromUser().getId());
                userToReply.changeActiveModule(ModuleEnum.SERVICE_MODULE.name());
                userService.saveUser(userToReply);
                telegramBot.sendMessage(userReplyMessage(userToReply.getChatId(), update.getMessage().getText()));
                mapOfServiceRequest.remove(chatId);
                serviceCallRequestService.deleteServiceCallRequestById(serviceCallRequest.getId());
                return successReplyMessage(chatId);
            } else {
                List<ServiceCallRequest> serviceCallRequestList = serviceCallRequestService.findAllServiceCallRequest();
                if (serviceCallRequestList.size() == 0) {
                    return noAvailableRequests(chatId);
                }
                return listOfRequests(chatId, serviceCallRequestList);
            }
        } else {
            if (update.getMessage().hasEntities()) {
                return infoMessage(chatId);
            }
            ServiceCallRequest serviceCallRequest = ServiceCallRequest.newBuilder()
                    .setFromUser(userEntity)
                    .setRequestText(update.getMessage().getText())
                    .build();
            serviceCallRequest = serviceCallRequestService.saveServiceCallRequest(serviceCallRequest);
            List<UserEntity> admins = userService.findAdmins();
            for (UserEntity admin : admins) {
                admin.changeActiveModule(ModuleEnum.SERVICE_MODULE.name());
                userService.saveUser(admin);
                telegramBot.sendMessage(incomingServiceCallMessage(admin.getChatId(), serviceCallRequest));
            }
            return successReviewRequest(chatId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        Long chatId = message.getChatId();
        String button = update.getCallbackQuery().getData();
        UserEntity admin = userService.findUserById(chatId);
        telegramBot.deleteMessage(message);
        try {
            Long.valueOf(button);
        } catch (Exception exception) {
            String serviceCallRequestId = StringUtils.substringAfter(button, ":");
            if (serviceCallRequestId.isEmpty()) {
                admin.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(admin);
                return null;
            }
            ServiceCallRequest serviceCallRequest = serviceCallRequestService.findServiceCallRequestById(Long.valueOf(serviceCallRequestId)).get();
            return serviceCallRequestMessage(chatId, serviceCallRequest);
        }
        Optional<ServiceCallRequest> serviceCallRequest = serviceCallRequestService.findServiceCallRequestById(Long.valueOf(button));
        if (serviceCallRequest.isPresent()) {
            mapOfServiceRequest.put(admin.getId(), serviceCallRequest.get().getId());
            return adminReplyMessage(admin.getId());
        }
        UserEntity userToReply = userService.findUserById(Long.valueOf(button));
        userToReply.changeUserBlockStatus(true);
        userService.saveUser(userToReply);
        serviceCallRequestService.deleteAllServiceCallRequestsByUser(userToReply.getId());
        return successBlockMessage(chatId, userToReply.getFullName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.SERVICE_MODULE;
    }

    /**
     * Сообщение админу о пришедшей заявки в техподдержку
     *
     * @param chatId             - id чата
     * @param serviceCallRequest - запрос в техподдержку
     * @return SendMessage
     */
    private SendMessage serviceCallRequestMessage(Long chatId, ServiceCallRequest serviceCallRequest) {
        UserEntity userEntity = serviceCallRequest.getFromUser();
        String text = MessageFormat.format(":hammer_and_wrench: Запрос от пользователя:{0} из {1}.{2}:information_source:{3}",
                userEntity.getFullName(), userEntity.getDepartment().getNameOfDepartment(), "\n", serviceCallRequest.getRequestText());
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, serviceCallRequest.getId().toString(), ButtonEnum.REPLY_USER.getButtonName());
        buttonKeyboard.addMessageButton(1, userEntity.getId().toString(), ButtonEnum.BLOCK_USER.getButtonName());
        buttonKeyboard.addMessageButton(2, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение о существующих заявках
     *
     * @param chatId                 - id чата админа
     * @param serviceCallRequestList - список запросов в техподдержку
     * @return SendMessage
     */
    private SendMessage listOfRequests(Long chatId, List<ServiceCallRequest> serviceCallRequestList) {
        String text = "Выберите одну из существующих заявок:";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        for (int i = 0; i < serviceCallRequestList.size(); i++) {
            String buttonName = MessageFormat.format("№{0}:{1}", i + 1, serviceCallRequestList.get(i).getFromUser().getFullName());
            String callBackData = MessageFormat.format("callBackData:{0}", serviceCallRequestList.get(i).getId());
            buttonKeyboard.addMessageButton(i, callBackData, buttonName);
        }
        buttonKeyboard.addMessageButton(serviceCallRequestList.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение админу об отсутствии новых заявок в техподдержку
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage noAvailableRequests(Long chatId) {
        String text = ":telephone: На данный момент нет ни одной заявки в техподдержку. Отдыхайте!";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение админу об успешной блокировке пользователя
     *
     * @param adminChatId         - id чата админа
     * @param blockedUserFullName - имя пользователя
     * @return SendMessage
     */
    private SendMessage successBlockMessage(Long adminChatId, String blockedUserFullName) {
        String text = MessageFormat.format("Пользователь {0} успешно заблокирован!", blockedUserFullName);
        return messageService.getMessage(text, adminChatId);
    }

    /**
     * Сообщение об успешно доставленном письме
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage successReplyMessage(Long chatId) {
        String text = "Письмо успешно доставлено!";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение пользователю об ответе техподдержкии
     *
     * @param chatId     - id чата
     * @param adminReply - ответ техподдержки
     * @return SendMessage
     */
    private SendMessage userReplyMessage(Long chatId, String adminReply) {
        String text = MessageFormat.format(":hammer_and_wrench: Вам поступил ответ по вашему запросу от техподдержки:{0}:information_source:{1}", "\n", adminReply);
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение админу о вводе ответа пользователю на его запрос
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage adminReplyMessage(Long chatId) {
        String text = "Введите ответ пользователю.";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение админу о новой заявке в техподдержку
     *
     * @param chatId             - id чата
     * @param serviceCallRequest - запрос в техподдержку
     * @return SendMessage
     */
    private SendMessage incomingServiceCallMessage(Long chatId, ServiceCallRequest serviceCallRequest) {
        UserEntity userEntity = serviceCallRequest.getFromUser();
        String text = MessageFormat.format(":hammer_and_wrench: Вам поступил запрос от пользователя:{0} из {1}.{2}:information_source:{3}",
                userEntity.getFullName(), userEntity.getDepartment().getNameOfDepartment(), "\n", serviceCallRequest.getRequestText());
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, serviceCallRequest.getId().toString(), ButtonEnum.REPLY_USER.getButtonName());
        buttonKeyboard.addMessageButton(1, userEntity.getId().toString(), ButtonEnum.BLOCK_USER.getButtonName());
        buttonKeyboard.addMessageButton(2, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение об успешной обработке заявки пользователя
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage successReviewRequest(Long chatId) {
        String text = EmojiParser.parseToUnicode("Ваша заявка была принята в работу!");
        return messageService.getMessage(text, chatId);
    }

    /**
     * Информационное сообщение при входе в модуль
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage infoMessage(Long chatId) {
        String text = MessageFormat.format(
                ":hammer_and_wrench: Вы находитесь в модуле техподдержки, здесь вы можете: {0}" +
                        ":black_small_square: Оставить заявку на участие в разработке бота {1}" +
                        ":black_small_square: Оставить отзыв по работе бота, а также сообщить об ошибках {2}" +
                        ":black_small_square: Предложить создание нового функционала {3}" +
                        ":incoming_envelope: Для этого просто напишите мне в чат, а я перешлю ваше сообщение в техподдержку. {4}" +
                        ":back: После отправки сообщения вы автоматически будете перенаправленны в главное меню {5}" +
                        ":exclamation: Помните, что все ваши сообщения обрабатываются реальными людьми. В случае спама - вы можете быть заблокированы в системе без предупреждения.",
                "\n", "\n", "\n", "\n", "\n", "\n");
        return messageService.getMessage(text, chatId);
    }
}
