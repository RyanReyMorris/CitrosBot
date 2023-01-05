package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.MessageService;

import java.text.MessageFormat;

/**
 * Модуль чат-бота, отвечающий за навигацию в главном меню
 *
 * @author eyakimov
 */
@Service
public class MainMenuModule implements Module {

    @Autowired
    private MessageService messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (update.getMessage().hasEntities()) {
            return infoMessage(chatId);
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return infoMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.MAIN_MENU_MODULE;
    }

    /**
     * Сообщение при первом входе в бота
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage infoMessage(Long chatId) {
        String text = MessageFormat.format(
                ":iphone: Добро пожаловать в CitrosBot!{0}" +
                        ":gear: Я - небольшой творческий проект с расширяемой системой модулей {1}" +
                        ":page_with_curl: В главном меню перечислен список всех доступных модулей{2}" +
                        ":birthday: Подключите модуль -День рождения-, чтобы раньше всех поздравить своих коллег-именинников{3}" +
                        ":clown_face: Перейдите в модуль -Анекдоты-, чтобы знать все самые свежие мемы в офисе{4}" +
                        ":envelope: Вы можете принять участие в моей разработке, оставить обо мне отзыв или предложить новый функционал. Для этого используйте команду /service {5}" +
                        ":bust_in_silhouette: Если хотите изменить данные о себе, используйте - /changeinfo {6}" +
                        ":information_source: Если вдруг что-то забудете, всегда можете написать мне /help {7}" +
                        "Удачи!", "\n", "\n", "\n", "\n", "\n", "\n", "\n", "\n");
        return messageService.getMessageWithMenuButtons(text, chatId, generateMenuButtons());
    }

    /**
     * Метод генерации кнопок главного меню
     *
     * @return ReplyKeyboardMarkup - кнопки
     */
    private ReplyKeyboardMarkup generateMenuButtons() {
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMenuButton(0, EmojiParser.parseToUnicode(":birthday: Анекдоты(/anecdote)"));
        buttonKeyboard.addMenuButton(1, EmojiParser.parseToUnicode(":birthday: День рождения(/birthday)"));
        buttonKeyboard.addMenuButton(2, EmojiParser.parseToUnicode(":bust_in_silhouette: Изменить данные о себе(/changeinfo)"));
        buttonKeyboard.addMenuButton(3, EmojiParser.parseToUnicode(":information_source: Помощь(/help)"));
        buttonKeyboard.addMenuButton(4, EmojiParser.parseToUnicode(":envelope: Техподдержка(/service)"));
        ReplyKeyboardMarkup keyboardMarkup = buttonKeyboard.getMenuButtons();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setInputFieldPlaceholder("Выберите интересующий вас модуль");
        return keyboardMarkup;
    }
}
