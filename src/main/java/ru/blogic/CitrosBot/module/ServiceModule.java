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
import ru.blogic.CitrosBot.service.KeyboardService;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceCallRequestService serviceCallRequestService;

    @Autowired
    private KeyboardService keyboardService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    /**
     * Мапа, хранящая в себе единственный запрос для конкретного админа. Ключ - id админа, значение - id запроса в техподдержку.
     * Мапа необходима для быстрого ответа админа пользователю по его вопросу.
     * После ответа админа, запись с его id удаляется из мапы.
     */
    private final Map<Long, Long> mapOfServiceRequest = new HashMap<>();

    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        if (userEntity.isAdmin()) {
            if (mapOfServiceRequest.get(userEntity.getId()) != null) {
                ServiceCallRequest serviceCallRequest = serviceCallRequestService.findServiceCallRequestById(mapOfServiceRequest.get(userEntity.getId())).get();
                UserEntity userToReply = userService.findUserById(serviceCallRequest.getFromUser().getId());
                userToReply.changeActiveModule(ModuleEnum.SERVICE_MODULE.name());
                userService.saveUser(userToReply);
                telegramBot.sendMessage(generateUserReplyMessage(userToReply, update.getMessage().getText()));
                mapOfServiceRequest.remove(userEntity.getId());
                serviceCallRequestService.deleteServiceCallRequestById(serviceCallRequest.getId());
                return generateSuccessReplyMessage(userEntity);
            } else {
                List<ServiceCallRequest> serviceCallRequestList = serviceCallRequestService.findAllServiceCallRequest();
                if (serviceCallRequestList.size() == 0) {
                    return generateNoAvailableRequests(userEntity);
                }
                return generateListOfRequests(userEntity, serviceCallRequestList);
            }
        } else {
            if (update.getMessage().getEntities() != null) {
                return generateInfoMessage(userEntity);
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
                telegramBot.sendMessage(generateIncomingServiceCallMessage(admin, serviceCallRequest));
            }
            return generateSuccessReviewRequest(userEntity);
        }
    }

    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        UserEntity admin = userService.findUserById(message.getChat().getId());
        String button = update.getCallbackQuery().getData();
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
            return serviceCallRequestMessage(admin, serviceCallRequest);
        }
        Optional<ServiceCallRequest> serviceCallRequest = serviceCallRequestService.findServiceCallRequestById(Long.valueOf(button));
        if (serviceCallRequest.isPresent()) {
            mapOfServiceRequest.put(admin.getId(), serviceCallRequest.get().getId());
            return generateAdminReplyMessage(admin);
        }
        UserEntity userToReply = userService.findUserById(Long.valueOf(button));
        userToReply.changeUserBlockStatus(true);
        userService.saveUser(userToReply);
        serviceCallRequestService.deleteAllServiceCallRequestsByUser(userToReply.getId());
        return generateSuccessBlockMessage(admin, userToReply);
    }

    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.SERVICE_MODULE;
    }

    private SendMessage serviceCallRequestMessage(UserEntity admin, ServiceCallRequest serviceCallRequest) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(admin.getChatId());
        UserEntity userEntity = serviceCallRequest.getFromUser();
        String text = MessageFormat.format(":hammer_and_wrench: Запрос от пользователя:{0} из {1}.{2}:information_source:{3}",
                userEntity.getFullName(), userEntity.getDepartment().getNameOfDepartment(), "\n", serviceCallRequest.getRequestText());
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        Map<String, String> firstLineOfButtons = new HashMap<>();
        firstLineOfButtons.put(serviceCallRequest.getId().toString(), ButtonEnum.REPLY_USER.getButtonName());
        mapOfMultilineButtons.put(1, firstLineOfButtons);
        Map<String, String> secondLineOfButtons = new HashMap<>();
        firstLineOfButtons.put(userEntity.getId().toString(), ButtonEnum.BLOCK_USER.getButtonName());
        mapOfMultilineButtons.put(2, secondLineOfButtons);
        Map<String, String> thirdLineOfButtons = new HashMap<>();
        thirdLineOfButtons.put(ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        mapOfMultilineButtons.put(3, thirdLineOfButtons);
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        return sendMessage;
    }


    private SendMessage generateListOfRequests(UserEntity admin, List<ServiceCallRequest> serviceCallRequestList) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(admin.getChatId());
        String text = EmojiParser.parseToUnicode("Выберите одну из существующих заявок:");
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        for (int i = 0; i < serviceCallRequestList.size(); i++) {
            Map<String, String> lineButtons = new HashMap<>();
            String buttonName = MessageFormat.format("№{0}:{1}", i + 1, serviceCallRequestList.get(i).getFromUser().getFullName());
            String callBackData = MessageFormat.format("callBackData:{0}", serviceCallRequestList.get(i).getId());
            lineButtons.put(callBackData, buttonName);
            mapOfMultilineButtons.put(i, lineButtons);
        }
        Map<String, String> lastLineOfButtons = new HashMap<>();
        lastLineOfButtons.put(ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        mapOfMultilineButtons.put(serviceCallRequestList.size(), lastLineOfButtons);
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateNoAvailableRequests(UserEntity admin) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(admin.getChatId());
        String text = EmojiParser.parseToUnicode(":telephone:На данный момент нет ни одной заявки в техподдержку. Отдыхайте!");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateSuccessBlockMessage(UserEntity admin, UserEntity blockedUser) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(admin.getChatId());
        String text = MessageFormat.format("Пользователь {0} успешно заблокирован!", blockedUser.getFullName());
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateSuccessReplyMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        sendMessage.setText("Письмо успешно доставлено!");
        return sendMessage;
    }

    private SendMessage generateUserReplyMessage(UserEntity userToReply, String adminReply) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userToReply.getChatId());
        String text = MessageFormat.format(":hammer_and_wrench: Вам поступил ответ по вашему запросу от техподдержки:{0}:information_source:{1}",
                "\n", adminReply);
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateAdminReplyMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        sendMessage.setText("Введите ответ пользователю.");
        return sendMessage;
    }

    private SendMessage generateIncomingServiceCallMessage(UserEntity admin, ServiceCallRequest serviceCallRequest) {
        SendMessage sendMessage = new SendMessage();
        UserEntity userEntity = serviceCallRequest.getFromUser();
        sendMessage.setChatId(admin.getChatId());
        String text = MessageFormat.format(":hammer_and_wrench: Вам поступил запрос от пользователя:{0} из {1}.{2}:information_source:{3}",
                userEntity.getFullName(), userEntity.getDepartment().getNameOfDepartment(), "\n", serviceCallRequest.getRequestText());
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        Map<String, String> firstLineOfButtons = new HashMap<>();
        firstLineOfButtons.put(serviceCallRequest.getId().toString(), ButtonEnum.REPLY_USER.getButtonName());
        mapOfMultilineButtons.put(1, firstLineOfButtons);
        Map<String, String> secondLineOfButtons = new HashMap<>();
        firstLineOfButtons.put(userEntity.getId().toString(), ButtonEnum.BLOCK_USER.getButtonName());
        mapOfMultilineButtons.put(2, secondLineOfButtons);
        Map<String, String> thirdLineOfButtons = new HashMap<>();
        thirdLineOfButtons.put(ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        mapOfMultilineButtons.put(3, thirdLineOfButtons);
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        return sendMessage;
    }

    private SendMessage generateSuccessReviewRequest(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode("Ваша заявка была принята в работу!");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateInfoMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format(
                ":hammer_and_wrench: Вы находитесь в модуле техподдержки, здесь вы можете: {0}" +
                        ":black_small_square: Оставить заявку на участие в разработке бота {1}" +
                        ":black_small_square: Оставить отзыв по работе бота, а также сообщить об ошибках {2}" +
                        ":black_small_square: Предложить создание нового функционала {3}" +
                        ":incoming_envelope: Для этого просто напишите мне в чат, а я перешлю ваше сообщение в техподдержку. {4}" +
                        ":back: После отправки сообщения вы автоматически будете перенаправленны в главное меню {5}" +
                        ":exclamation: Помните, что все ваши сообщения обрабатываются реальными людьми. В случае спама - вы можете быть заблокированы в системе без предупреждения.",
                "\n", "\n", "\n", "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateUnknownCommandMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode(":warning: Ошибка: данная команда недоступна в данным момент или же неизвестна");
        sendMessage.setText(text);
        return sendMessage;
    }

}
