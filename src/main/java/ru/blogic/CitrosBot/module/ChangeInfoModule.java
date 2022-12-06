package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.event.CustomApplicationEvent;
import ru.blogic.CitrosBot.repository.UserRepository;
import ru.blogic.CitrosBot.service.KeyboardServiceImpl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Модуль чат-бота, отвечающий за изменение данных пользователяя
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class ChangeInfoModule implements Module {

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
        user.changeFullName(update.getMessage().getText());
        userRepository.save(user);
        return generateAfterInfoChangeText(user);
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
            case ("YES_CHANGE_INFO_MODULE"):
                if (!user.isRegistered()){
                    user.changeUserStatus(ModuleEnum.MAIN_MENU_MODULE.name());
                    user.changeRegistrationStatus(true);
                    userRepository.save(user);
                    sendMessage = generateCancelMessageIfUserNotRegistered(user);
                } else {
                    user.changeUserStatus(ModuleEnum.MAIN_MENU_MODULE.name());
                    userRepository.save(user);
                    sendMessage = generateCancelMessage(user);
                }
                break;
            case ("NO_CHANGE_INFO_MODULE"):
                sendMessage = generateRepeatMessage(user);
                break;
        }
        return sendMessage;
    }

    private SendMessage generateRepeatMessage(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Хорошо!{0}Напишите пожалуйста свое ФИО/Имя/Кличку/Погоняло заново :new_moon_with_face:", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateCancelMessage(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Вас понял, {0} :new_moon_with_face: {1}Ваши данные изменены", user.getFullName(), "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateAfterInfoChangeText(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format("Так значит вас зовут {0}? :new_moon_with_face: {1}Скажите, я вас правильно понял? ", user.getFullName(), "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<String, String> mapOfButtons = new HashMap<>();
        mapOfButtons.put("YES_CHANGE_INFO_MODULE", "Да");
        mapOfButtons.put("NO_CHANGE_INFO_MODULE", "Нет");
        sendMessage.setReplyMarkup(keyboardService.getInlineButtons(mapOfButtons));
        return sendMessage;
    }

    private SendMessage generateCancelMessageIfUserNotRegistered(User user){
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.CHANGE_INFO_MODULE;
    }
}
