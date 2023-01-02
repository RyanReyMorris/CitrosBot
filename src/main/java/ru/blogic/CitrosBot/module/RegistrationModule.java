package ru.blogic.CitrosBot.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;

/**
 * Модуль чат-бота, отвечающий за регистрацию пользователя.
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class RegistrationModule implements Module {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        UserEntity userEntity = userService.findUserById(chatId);
        userEntity.changeActiveModule(ModuleEnum.CHANGE_INFO_MODULE.name());
        userService.saveUser(userEntity);
        return welcomeText(chatId, userEntity.getFullName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        return messageService.getErrorMessage(chatId);
    }

    /**
     * Первое сообщение при регистраии в боте.
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
        buttonKeyboard.addMessageButton(0, ButtonEnum.START_CHANGE_INFO_MODULE.name(), ButtonEnum.START_CHANGE_INFO_MODULE.getButtonName());
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
