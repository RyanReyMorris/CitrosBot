package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.Department;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.DepartmentService;
import ru.blogic.CitrosBot.service.KeyboardService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Модуль чат-бота, отвечающий за изменение данных пользователяя
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class ChangeInfoModule implements Module {
    /**
     * Паттерн формата даты дня рождения
     */
    private final String patternOfDate = "yyyy-MM-dd";

    @Autowired
    private UserService userService;

    @Autowired
    private KeyboardService keyboardService;

    @Autowired
    private DepartmentService departmentService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        switch (ButtonEnum.valueOf(userEntity.getUserInfoStatus())) {
            case START_CHANGE_INFO_MODULE:
                return generateMessageForInfoChanging(userEntity);
            case CHANGE_INFO_NAME:
                telegramBot.deleteMessage(update.getMessage());
                userEntity.changeFullName(update.getMessage().getText());
                userService.saveUser(userEntity);
                if (userEntity.isRegistered()) {
                    return successInfoChange(userEntity);
                }
                return generateDepartmentChoiceMessage(userEntity);
            case CHANGE_INFO_BIRTHDAY:
                telegramBot.deleteMessage(update.getMessage());
                try {
                    Date birthdayDate = new SimpleDateFormat(patternOfDate).parse(update.getMessage().getText());
                    userEntity.changeBirthday(birthdayDate);
                    userService.saveUser(userEntity);
                    if (userEntity.isRegistered()) {
                        return successInfoChange(userEntity);
                    }
                    return generateTimeZoneMessage(userEntity);
                } catch (ParseException e) {
                    return generateErrorBirthdayParseMessage(userEntity);
                }
            case CHANGE_INFO_TIME_ZONE:
                telegramBot.deleteMessage(update.getMessage());
                try {
                    String timeZone = update.getMessage().getText();
                    ZoneId zoneId = ZoneId.of(timeZone);
                    userEntity.changeTimeZone(timeZone);
                    userService.saveUser(userEntity);
                    if (userEntity.isRegistered()) {
                        return successInfoChange(userEntity);
                    }
                    userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                    userEntity.changeRegistrationStatus(true);
                    userService.saveUser(userEntity);
                    return generateSuccessRegistrationMessage(userEntity);
                } catch (Exception exception) {
                    return generateErrorTimeZoneParseMessage(userEntity);
                }
        }
        return generateUnknownCommandMessage(userEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        UserEntity userEntity = userService.findUserById(message.getChat().getId());
        String button = update.getCallbackQuery().getData();
        if (departmentService.isExistingDepartment(button)) {
            telegramBot.deleteMessage(message);
            Department department = departmentService.getDepartmentByName(button);
            userEntity.changeDepartment(department);
            userService.saveUser(userEntity);
            if (userEntity.isRegistered()) {
                return successInfoChange(userEntity);
            }
            return generateBirthdayMessage(userEntity);
        }
        switch (ButtonEnum.valueOf(button)) {
            case START_CHANGE_INFO_MODULE:
                telegramBot.deleteMessage(message);
                return generateStartMessage(userEntity);
            case CHANGE_INFO_NAME:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
                userService.saveUser(userEntity);
                return changeFullNameMessage(userEntity);
            case CHANGE_INFO_BIRTHDAY:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
                userService.saveUser(userEntity);
                return changeBirthdayMessage(userEntity);
            case CHANGE_INFO_DEPARTMENT:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_DEPARTMENT.name());
                userService.saveUser(userEntity);
                return changeDepartmentMessage(userEntity);
            case CHANGE_INFO_TIME_ZONE:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
                userService.saveUser(userEntity);
                return changeTimeZoneMessage(userEntity);
            case EXIT_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                userService.saveUser(userEntity);
                return null;
        }
        return generateUnknownCommandMessage(userEntity);
    }

    private SendMessage generateMessageForInfoChanging(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode("Мир - изменчив, скажите, что конкретно вы хотите изменить в своих данных?");
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        List<ButtonEnum> userInfoButtons = ButtonEnum.getUserInfoButtons();
        for (int i = 0; i < userInfoButtons.size(); i++) {
            Map<String, String> lineButtons = new HashMap<>();
            lineButtons.put(userInfoButtons.get(i).name(), userInfoButtons.get(i).getButtonName());
            mapOfMultilineButtons.put(i, lineButtons);
        }
        Map<String, String> lineButtons = new HashMap<>();
        lineButtons.put(ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        mapOfMultilineButtons.put(userInfoButtons.size(), lineButtons);
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
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

    private SendMessage successInfoChange(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = "Данные успешно изменены, желаете изменить что-нибудь еще?";
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        List<ButtonEnum> userInfoButtons = ButtonEnum.getUserInfoButtons();
        for (int i = 0; i < userInfoButtons.size(); i++) {
            Map<String, String> lineButtons = new HashMap<>();
            lineButtons.put(userInfoButtons.get(i).name(), userInfoButtons.get(i).getButtonName());
            mapOfMultilineButtons.put(i, lineButtons);
        }
        Map<String, String> lineButtons = new HashMap<>();
        lineButtons.put(ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        mapOfMultilineButtons.put(userInfoButtons.size(), lineButtons);
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage changeTimeZoneMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = "Введите свой часовой пояс в формате регион/город, к примеру: Asia/Yekaterinburg или Europe/Moscow.";
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage changeDepartmentMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = "Выберите отдел, в котором вы работаете:";
        sendMessage.setText(text);
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        List<Department> departmentList = departmentService.getAllDepartments();
        for (int i = 0; i < departmentList.size(); i++) {
            Map<String, String> lineButtons = new HashMap<>();
            lineButtons.put(departmentList.get(i).getNameOfDepartment(), departmentList.get(i).getNameOfDepartment());
            mapOfMultilineButtons.put(i, lineButtons);
        }
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        return sendMessage;
    }

    private SendMessage changeBirthdayMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Введите дату своего рождения в формате {0}", patternOfDate);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage changeFullNameMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode("Напишите свое имя (ФИО) так, чтобы ваши коллеги могли вас узнать :clipboard:");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateSuccessRegistrationMessage(UserEntity userEntity) {
        userEntity.changeUserInfoStatus(ButtonEnum.START_CHANGE_INFO_MODULE.name());
        userService.saveUser(userEntity);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Ши-кар-но :grin:{0}Да начнется веселье!", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put(ButtonEnum.START_MAIN_MENU_MODULE.name(), ButtonEnum.START_MAIN_MENU_MODULE.getButtonName());
        sendMessage.setReplyMarkup(keyboardService.getInlineButtons(mapOfButtons));
        return sendMessage;
    }

    private SendMessage generateErrorTimeZoneParseMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode("Не понял вас :disappointed_relieved:, попробуйте ввести часовой пояс в формате регион/город еще раз");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateTimeZoneMessage(UserEntity userEntity) {
        userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
        userService.saveUser(userEntity);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode("4) И последний шаг, введите свой часовой пояс" +
                " в формате регион/город, к примеру: Asia/Yekaterinburg или Europe/Moscow.");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateErrorBirthdayParseMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Не понял вас :disappointed_relieved:, попробуйте ввести дату в формате {0} еще раз", patternOfDate);
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateBirthdayMessage(UserEntity userEntity) {
        userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
        userService.saveUser(userEntity);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Вам повезло работотать в этом отделе!{0}3)Введите дату своего рождения в формате {1}", "\n", patternOfDate);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateDepartmentChoiceMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Понял вас, {0}!{1}2)Выберите отдел, в котором вы работаете:", userEntity.getFullName(), "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<Integer, Map<String, String>> mapOfMultilineButtons = new HashMap<>();
        List<Department> departmentList = departmentService.getAllDepartments();
        for (int i = 0; i < departmentList.size(); i++) {
            Map<String, String> lineButtons = new HashMap<>();
            lineButtons.put(departmentList.get(i).getNameOfDepartment(), departmentList.get(i).getNameOfDepartment());
            mapOfMultilineButtons.put(i, lineButtons);
        }
        sendMessage.setReplyMarkup(keyboardService.getMultilineButtons(mapOfMultilineButtons));
        return sendMessage;
    }

    private SendMessage generateStartMessage(UserEntity userEntity) {
        userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
        userService.saveUser(userEntity);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format("Персональные данные можно будет изменить в любой момент!{0}" +
                "В дальнейшем я обязательно расскажу вам, как это сделать,{1}" +
                "так что не стоит переживать, если вдруг ошибетесь.{2}" +
                "Давайте начнем по порядку:{3}1) Напишите свое имя (ФИО) так, чтобы ваши коллеги могли вас узнать :clipboard:", "\n", "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.CHANGE_INFO_MODULE;
    }
}
