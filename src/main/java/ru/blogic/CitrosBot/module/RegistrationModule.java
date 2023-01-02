package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.KeyboardService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
    private KeyboardService keyboardService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;


    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        userEntity.changeActiveModule(ModuleEnum.CHANGE_INFO_MODULE.name());
        userService.saveUser(userEntity);
        return generateWelcomeText(update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        return generateUnknownCommandMessage(userEntity);
    }

    private SendMessage generateUnknownCommandMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode(":warning: Ошибка: данная команда недоступна в данным момент или же неизвестна");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateWelcomeText(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        String text = MessageFormat.format("Привет, {0}! :new_moon_with_face: {1}Меня зовут CitrosBot :robot_face: {2}" +
                "Прежде чем начать, вашим коллегам (да и мне тоже) будет проще, если вы введете свои актуальные данные.{3}" +
                "Ну что-ж, давайте знакомиться? :eyes:", update.getMessage().getFrom().getFirstName(), "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put(ButtonEnum.START_CHANGE_INFO_MODULE.name(), ButtonEnum.START_CHANGE_INFO_MODULE.getButtonName());
        sendMessage.setReplyMarkup(keyboardService.getInlineButtons(mapOfButtons));
        return sendMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.REGISTRATION_MODULE;
    }
}
