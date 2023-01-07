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
        telegramBot.deleteMessage(message);
        switch (ButtonEnum.valueOf(userEntity.getUserInfoStatus())) {
            case START_CHANGE_INFO_MODULE:
                return startInfoChanging(userEntity);
            case CHANGE_INFO_NAME:
                userEntity.changeFullName(textOfMessage);
                userService.saveUser(userEntity);
                return successInfoChange(chatId);
            case CHANGE_INFO_BIRTHDAY:
                try {
                    LocalDate birthdayDate = LocalDate.parse(textOfMessage);
                    userEntity.changeBirthday(birthdayDate);
                    userService.saveUser(userEntity);
                    return successInfoChange(chatId);
                } catch (Exception e) {
                    return errorBirthdayParseMessage(chatId);
                }
            case CHANGE_INFO_TIME_ZONE:
                try {
                    ZoneId.of(textOfMessage);
                    userEntity.changeTimeZone(textOfMessage);
                    userService.saveUser(userEntity);
                    return successInfoChange(chatId);
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
            userService.saveUser(userEntity);
            return successInfoChange(chatId);
        }
        switch (ButtonEnum.valueOf(button)) {
            case CHANGE_INFO_NAME:
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_NAME.name());
                userService.saveUser(userEntity);
                return changeFullName(chatId);
            case CHANGE_INFO_BIRTHDAY:
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_BIRTHDAY.name());
                userService.saveUser(userEntity);
                return changeBirthday(chatId);
            case CHANGE_INFO_DEPARTMENT:
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_DEPARTMENT.name());
                userService.saveUser(userEntity);
                return changeDepartment(chatId);
            case CHANGE_INFO_TIME_ZONE:
                userEntity.changeUserInfoStatus(ButtonEnum.CHANGE_INFO_TIME_ZONE.name());
                userService.saveUser(userEntity);
                return changeTimeZone(chatId);
            case EXIT_MODULE:
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
     * @param userEntity - пользователь
     * @return - SendMessage
     */
    private SendMessage startInfoChanging(UserEntity userEntity) {
        String text = MessageFormat.format("Персональна информация:{0}" +
                        "1)Имя: {1}{2}" +
                        "2)Дата рождения: {3}{4}" +
                        "3)Часовой пояс: {5}{6}" +
                        "4)Отдел: {7}{8}" +
                        "Скажите, что конкретно вы хотите изменить в своих данных?",
                "\n", userEntity.getFullName(), "\n", userEntity.getBirthday(), "\n", userEntity.getTimeZone(), "\n", userEntity.getDepartment().getNameOfDepartment(), "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<ButtonEnum> userInfoButtons = ButtonEnum.getUserInfoButtons();
        for (int i = 0; i < userInfoButtons.size(); i++) {
            buttonKeyboard.addMessageButton(i, userInfoButtons.get(i).name(), userInfoButtons.get(i).getButtonName());
        }
        buttonKeyboard.addMessageButton(userInfoButtons.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, userEntity.getId(), buttonKeyboard.getMessageButtons());
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
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.CHANGE_INFO_MODULE;
    }
}
