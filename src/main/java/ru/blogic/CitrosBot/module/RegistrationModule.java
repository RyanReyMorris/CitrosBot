package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.User;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        User user = userService.findUserById(update.getMessage().getChat().getId());
        user.changeUserStatus(ModuleEnum.CHANGE_INFO_MODULE.name());
        userService.saveUser(user);
        return generateWelcomeText(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        User user = userService.findUserById(update.getMessage().getChat().getId());
        return generateUnknownCommandMessage(user);
    }

    private SendMessage generateUnknownCommandMessage(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = EmojiParser.parseToUnicode("К сожалению, я вас не понимаю, повторите попытку :pensive: ");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateWelcomeText(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Привет, {0}! :new_moon_with_face: {1}Меня зовут CitrosBot :robot_face: {2}" +
                "Прежде чем начать, вашим коллегам (да и мне тоже) будет проще, если вы введете свои актуальные данные.{3}" +
                "Ну что-ж, давайте знакомиться? :eyes:", user.getFullName(), "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put("START_REGISTRATION_MODULE", "Давайте!");
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
