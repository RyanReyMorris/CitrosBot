package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.repository.UserRepository;
import ru.blogic.CitrosBot.service.KeyboardServiceImpl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private UserRepository userRepository;

    @Autowired
    private KeyboardServiceImpl keyboardService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        User user = userRepository.findById(update.getMessage().getChat().getId()).get();
        return generateWelcomeText(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        User user = userRepository.findById(update.getCallbackQuery().getMessage().getChat().getId()).get();
        String button = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        switch (button) {
            case ("YES_REGISTRATION_MODULE"):
                user.changeUserStatus(ModuleEnum.CHANGE_INFO_MODULE.name());
                userRepository.save(user);
                sendMessage = generateBeforeInfoChangeText(user);
                break;
            case ("NO_REGISTRATION_MODULE"):
                user.changeUserStatus(ModuleEnum.MAIN_MENU_MODULE.name());
                user.changeRegistrationStatus(true);
                userRepository.save(user);
                sendMessage = generateCancelMessage(user);
                break;
        }
        return sendMessage;
    }

    private SendMessage generateCancelMessage(User user){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Отлично, {0} :new_moon_with_face: {1}Да начнется веселье!", user.getFullName(),"\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put("START_MAIN_MENU_MODULE", "Начать!");
        sendMessage.setReplyMarkup(keyboardService.getInlineButtons(mapOfButtons));
        return sendMessage;
    }

    private SendMessage generateBeforeInfoChangeText(User user){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Отлично! :new_moon_with_face:  {0}  {1}Скажите, как я могу к вам обращаться? ", "\n","\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateWelcomeText(User user){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Привет, {0}! :new_moon_with_face: {1}Вашим коллегам будет проще, если вы введете свои " +
                "актуальные данные.{2}Желаете их изменить?", user.getFullName(), "\n","\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put("YES_REGISTRATION_MODULE", "Да");
        mapOfButtons.put("NO_REGISTRATION_MODULE", "Нет");
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
