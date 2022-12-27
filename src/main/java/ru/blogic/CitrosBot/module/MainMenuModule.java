package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.KeyboardService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Модуль чат-бота, отвечающий за навигацию в главном меню
 *
 * @author eyakimov
 */
@Service
public class MainMenuModule implements Module {

    @Autowired
    private UserService userService;

    @Autowired
    private KeyboardService keyboardService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        User user = userService.findUserById(update.getCallbackQuery().getMessage().getChat().getId());
        String button = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        switch (button) {
            case ("START_MAIN_MENU_MODULE"):
                sendMessage = generateInfoMessage(user);
                break;
        }
        return sendMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.MAIN_MENU_MODULE;
    }

    private SendMessage generateInfoMessage(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = MessageFormat.format(
                "Добро пожаловать в CitrosBot! :new_moon_with_face: " +
                        "{0}Снизу перечисленны все доступные модули {1}" +
                        "Если вдруг что-то забудете, всегда можете написать мне /info", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        Map<Integer, List<String>> mapOfButtons = new HashMap<>();
        mapOfButtons.put(1, Collections.singletonList(EmojiParser.parseToUnicode(":lower_left_fountain_pen: Изменить данные о себе(/changeinfo)")));
        ReplyKeyboardMarkup keyboardMarkup = keyboardService.getMenuButtons(mapOfButtons);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setInputFieldPlaceholder("Выберите интересующий модуль");
        keyboardMarkup.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
