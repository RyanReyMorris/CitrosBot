package ru.blogic.CitrosBot.module;

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
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.DepartmentService;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Модуль чат-бота, отвечающий за изменение данных пользователя
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
    private DepartmentService departmentService;

    @Autowired
    private MessageService messageService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Message message = update.getMessage();
        String textOfMessage = message.getText();
        Long chatId = message.getChatId();
        UserEntity userEntity = userService.findUserById(chatId);
        switch (ButtonEnum.valueOf(userEntity.getUserInfoStatus())) {
            case START_CHANGE_INFO_MODULE:
                return startInfoChanging(chatId);
            case CHANGE_INFO_NAME:
                telegramBot.deleteMessage(message);
                userEntity.changeFullName(textOfMessage);
                userService.saveUser(userEntity);
                if (userEntity.isRegistered()) {
                    return successInfoChange(chatId);
                }
                return registrationDepartment(chatId, userEntity.getFullName());
            case CHANGE_INFO_BIRTHDAY:
                telegramBot.deleteMessage(message);
                try {
                    Date birthdayDate = new SimpleDateFormat(patternOfDate).parse(textOfMessage);
                    userEntity.changeBirthday(birthdayDate);
                    userService.saveUser(userEntity);
                    if (userEntity.isRegistered()) {
                        return successInfoChange(chatId);
                    }
                    userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
                    userService.saveUser(userEntity);
                    return registrationTimeZone(chatId);
                } catch (ParseException e) {
                    return errorBirthdayParseMessage(chatId);
                }
            case CHANGE_INFO_TIME_ZONE:
                telegramBot.deleteMessage(message);
                try {
                    ZoneId.of(textOfMessage);
                    userEntity.changeTimeZone(textOfMessage);
                    userService.saveUser(userEntity);
                    if (userEntity.isRegistered()) {
                        return successInfoChange(chatId);
                    }
                    userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                    userEntity.changeUserInfoStatus(ButtonEnum.START_CHANGE_INFO_MODULE.name());
                    userEntity.changeRegistrationStatus(true);
                    userService.saveUser(userEntity);
                    return successRegistration(chatId);
                } catch (Exception exception) {
                    return errorTimeZoneParseMessage(chatId);
                }
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        Long chatId = message.getChatId();
        UserEntity userEntity = userService.findUserById(chatId);
        String button = update.getCallbackQuery().getData();
        if (departmentService.isExistingDepartment(button)) {
            telegramBot.deleteMessage(message);
            Department department = departmentService.getDepartmentByName(button);
            userEntity.changeDepartment(department);
            userService.saveUser(userEntity);
            if (userEntity.isRegistered()) {
                return successInfoChange(chatId);
            }
            userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
            userService.saveUser(userEntity);
            return registrationBirthday(chatId);
        }
        switch (ButtonEnum.valueOf(button)) {
            case START_CHANGE_INFO_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
                userService.saveUser(userEntity);
                return registrationStart(chatId);
            case CHANGE_INFO_NAME:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
                userService.saveUser(userEntity);
                return changeFullName(chatId);
            case CHANGE_INFO_BIRTHDAY:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
                userService.saveUser(userEntity);
                return changeBirthday(chatId);
            case CHANGE_INFO_DEPARTMENT:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_DEPARTMENT.name());
                userService.saveUser(userEntity);
                return changeDepartment(chatId);
            case CHANGE_INFO_TIME_ZONE:
                telegramBot.deleteMessage(message);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
                userService.saveUser(userEntity);
                return changeTimeZone(chatId);
            case EXIT_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                userService.saveUser(userEntity);
                return null;
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * Первое сообщение при входе в модуль после регистрации
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage startInfoChanging(Long chatId) {
        String text = "Мир - изменчив, скажите, что конкретно вы хотите изменить в своих данных?";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<ButtonEnum> userInfoButtons = ButtonEnum.getUserInfoButtons();
        for (int i = 0; i < userInfoButtons.size(); i++) {
            buttonKeyboard.addMessageButton(i, userInfoButtons.get(i).name(), userInfoButtons.get(i).getButtonName());
        }
        buttonKeyboard.addMessageButton(userInfoButtons.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение об успешном изменении данных
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage successInfoChange(Long chatId) {
        String text = "Данные успешно изменены, желаете изменить что-нибудь еще?";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<ButtonEnum> userInfoButtons = ButtonEnum.getUserInfoButtons();
        for (int i = 0; i < userInfoButtons.size(); i++) {
            buttonKeyboard.addMessageButton(i, userInfoButtons.get(i).name(), userInfoButtons.get(i).getButtonName());
        }
        buttonKeyboard.addMessageButton(userInfoButtons.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение об изменении часового пояса
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage changeTimeZone(Long chatId) {
        String text = "Введите свой часовой пояс в формате регион/город, к примеру: Asia/Yekaterinburg или Europe/Moscow.";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение об изменении отдела
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage changeDepartment(Long chatId) {
        String text = "Выберите отдел, в котором вы работаете:";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<Department> departmentList = departmentService.getAllDepartments();
        for (int i = 0; i < departmentList.size(); i++) {
            buttonKeyboard.addMessageButton(i, departmentList.get(i).getNameOfDepartment(), departmentList.get(i).getNameOfDepartment());
        }
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение об изменении даты рождения
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage changeBirthday(Long chatId) {
        String text = MessageFormat.format("Введите дату своего рождения в формате {0}", patternOfDate);
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение об изменении полного имени
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage changeFullName(Long chatId) {
        String text = "Напишите свое имя (ФИО) так, чтобы ваши коллеги могли вас узнать :clipboard:";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение о неверно введенном часовом поясе
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage errorTimeZoneParseMessage(Long chatId) {
        String text = "Не понял вас :disappointed_relieved:, попробуйте ввести часовой пояс в формате регион/город еще раз";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение о неверно введенной дате рождения
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage errorBirthdayParseMessage(Long chatId) {
        String text = MessageFormat.format("Не понял вас :disappointed_relieved:, попробуйте ввести дату в формате {0} еще раз", patternOfDate);
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение об успешном окончании регистрации
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage successRegistration(Long chatId) {
        String text = MessageFormat.format("Ши-кар-но :grin:{0}Да начнется веселье!", "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.START_MAIN_MENU_MODULE.name(), ButtonEnum.START_MAIN_MENU_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Четвертое сообщение при регистрации. Ввод часового пояса
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage registrationTimeZone(Long chatId) {
        String text = "4) И последний шаг, введите свой часовой пояс в формате регион/город, к примеру: Asia/Yekaterinburg или Europe/Moscow.";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Третье сообщение при регистрации. Ввод даты рождения
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage registrationBirthday(Long chatId) {
        String text = MessageFormat.format("Вам повезло работотать в этом отделе!{0}3)Введите дату своего рождения в формате {1}", "\n", patternOfDate);
        return messageService.getMessage(text, chatId);
    }

    /**
     * Второе сообщение при регистрации. Ввод отдела
     *
     * @param chatId   - id чата
     * @param fullName - имя пользователя
     * @return - SendMessage
     */
    private SendMessage registrationDepartment(Long chatId, String fullName) {
        String text = MessageFormat.format("Понял вас, {0}!{1}2)Выберите отдел, в котором вы работаете:", fullName, "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<Department> departmentList = departmentService.getAllDepartments();
        for (int i = 0; i < departmentList.size(); i++) {
            buttonKeyboard.addMessageButton(i, departmentList.get(i).getNameOfDepartment(), departmentList.get(i).getNameOfDepartment());
        }
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Первое сообщение при регистрации. Ввод имени.
     *
     * @param chatId - id чата
     * @return - SendMessage
     */
    private SendMessage registrationStart(Long chatId) {
        String text = MessageFormat.format("Персональные данные можно будет изменить в любой момент!{0}" +
                "В дальнейшем я обязательно расскажу вам, как это сделать,{1}" +
                "так что не стоит переживать, если вдруг ошибетесь.{2}" +
                "Давайте начнем по порядку:{3}1) Напишите свое имя (ФИО) так, чтобы ваши коллеги могли вас узнать :clipboard:", "\n", "\n", "\n", "\n");
        return messageService.getMessage(text, chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.CHANGE_INFO_MODULE;
    }
}
