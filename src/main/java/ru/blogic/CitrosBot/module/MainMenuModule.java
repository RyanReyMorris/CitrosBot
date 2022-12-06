package ru.blogic.CitrosBot.module;

import com.vdurmont.emoji.EmojiParser;
import jakarta.annotation.Resource;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.event.CustomApplicationEvent;
import ru.blogic.CitrosBot.repository.UserRepository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Модуль чат-бота, отвечающий за навигацию в главном меню
 *
 * @author eyakimov
 */
@Service
public class MainMenuModule extends BotCommand implements Module {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void setCommand(@NonNull String command) {
        super.setCommand(command);
    }

    @Override
    public void setDescription(@NonNull String description) {
        super.setDescription(description);
    }

    public MainMenuModule() {
        super();
    }

    public MainMenuModule(@NonNull String command, @NonNull String description) {
        super(command, description);
    }

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
        User user = userRepository.findById(update.getCallbackQuery().getMessage().getChat().getId()).get();
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
                        "Если вдруг что-то забудете, всегда можете написать мне /info или же зайти в мое меню", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
