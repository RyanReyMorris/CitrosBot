package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.blogic.CitrosBot.entity.UserEntity;
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
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        if (update.getMessage().getEntities().size() != 0) {
            return generateInfoMessage(userEntity);
        }
        return generateUnknownCommandMessage(userEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        UserEntity userEntity = userService.findUserById(update.getCallbackQuery().getMessage().getChat().getId());
        String button = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        switch (button) {
            case ("START_MAIN_MENU_MODULE"):
                sendMessage = generateInfoMessage(userEntity);
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

    private SendMessage generateInfoMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format(
                ":iphone: Добро пожаловать в CitrosBot!{0}" +
                        ":gear: Я - небольшой творческий проект с расширяемой системой модулей  {1}" +
                        ":page_with_curl: В главном меню перечислен список всех доступных модулей {2}" +
                        ":envelope: Вы можете принять участие в моей разработке, оставить обо мне отзыв или предложить новый функционал. Для этого используйте команду /service {3}" +
                        ":bust_in_silhouette: Если хотите изменить данные о себе, используйте - /changeinfo {4}" +
                        ":information_source: Если вдруг что-то забудете, всегда можете написать мне /help {5}" +
                        "Удачи!", "\n", "\n", "\n", "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(generateMenuButtons());
        return sendMessage;
    }

    private SendMessage generateUnknownCommandMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode(":warning: Ошибка: данная команда недоступна в данным момент или же неизвестна");
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(generateMenuButtons());
        return sendMessage;
    }

    private ReplyKeyboardMarkup generateMenuButtons() {
        Map<Integer, List<String>> mapOfButtons = new HashMap<>();
        mapOfButtons.put(1, Collections.singletonList(EmojiParser.parseToUnicode(":bust_in_silhouette: Изменить данные о себе(/changeinfo)")));
        mapOfButtons.put(2, Collections.singletonList(EmojiParser.parseToUnicode(":information_source: Помощь(/help)")));
        mapOfButtons.put(3, Collections.singletonList(EmojiParser.parseToUnicode(":envelope: Техподдержка(/service)")));
        ReplyKeyboardMarkup keyboardMarkup = keyboardService.getMenuButtons(mapOfButtons);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setInputFieldPlaceholder("Выберите интересующий вас модуль");
        return keyboardMarkup;
    }
}
