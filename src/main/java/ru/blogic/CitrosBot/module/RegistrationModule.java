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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Модуль чат-бота, отвечающий за регистрацию пользователя.
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class RegistrationModule implements Module {
    /**
     * Паттерн формата даты дня рождения
     */
    private final String patternOfDate = "yyyy-MM-dd";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

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
        Message message = update.getMessage();
        String textOfMessage = message.getText();
        Long chatId = message.getChatId();
        UserEntity userEntity = userService.findUserById(chatId);
        telegramBot.deleteMessage(message);
        if (message.hasEntities()) {
            return welcomeText(chatId, userEntity.getFullName());
        }
        if (userEntity.getUserInfoStatus() == null) {
            return messageService.getErrorMessage(chatId);
        }
        switch (ButtonEnum.valueOf(userEntity.getUserInfoStatus())) {
            case CHANGE_INFO_NAME:
                userEntity.changeFullName(textOfMessage);
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_DEPARTMENT.name());
                userService.saveUser(userEntity);
                return registrationDepartment(chatId, userEntity.getFullName());
            case CHANGE_INFO_DEPARTMENT:
                return messageService.getErrorMessage(chatId);
            case CHANGE_INFO_BIRTHDAY:
                try {
                    LocalDate birthdayDate = LocalDate.parse(textOfMessage);
                    userEntity.changeBirthday(birthdayDate);
                    userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
                    userService.saveUser(userEntity);
                    return registrationTimeZone(chatId);
                } catch (Exception e) {
                    return errorBirthdayParseMessage(chatId);
                }
            case CHANGE_INFO_TIME_ZONE:
                try {
                    ZoneId.of(textOfMessage);
                    userEntity.changeTimeZone(textOfMessage);
                    userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
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
        telegramBot.deleteMessage(message);
        if (departmentService.isExistingDepartment(button)) {
            Department department = departmentService.getDepartmentByName(button);
            userEntity.changeDepartment(department);
            userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
            userService.saveUser(userEntity);
            return registrationBirthday(chatId);
        }
        userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
        userService.saveUser(userEntity);
        return registrationStart(chatId);
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
        String text = MessageFormat.format("Вам повезло работать в этом отделе!{0}3)Введите дату своего рождения в формате {1}", "\n", patternOfDate);
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
     * Первое сообщение при регистрации в боте.
     *
     * @param chatId   id чата
     * @param fullName - имя пользователя
     * @return SendMessage
     */
    private SendMessage welcomeText(Long chatId, String fullName) {
        String text = MessageFormat.format("Привет, {0}! :new_moon_with_face: {1}Меня зовут CitrosBot :robot_face: {2}" +
                "Прежде чем начать, вашим коллегам (да и мне тоже) будет проще, если вы введете свои актуальные данные.{3}" +
                "Ну что-ж, давайте знакомиться? :eyes:", fullName, "\n", "\n", "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.START_REGISTRATION_MODULE.name(), ButtonEnum.START_REGISTRATION_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.REGISTRATION_MODULE;
    }
}
