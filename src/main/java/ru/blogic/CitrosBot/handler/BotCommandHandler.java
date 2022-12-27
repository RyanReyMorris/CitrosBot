package ru.blogic.CitrosBot.handler;

import com.vdurmont.emoji.EmojiParser;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.BotCommandEnum;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Хэндлер команд
 *
 * @author eyakimov
 */
@Component
public class BotCommandHandler implements Handler {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("allModules")
    private Map<ModuleEnum, Module> allModules;

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        User user = userService.findUserById(update.getMessage().getChat().getId());
        BotCommandEnum command = BotCommandEnum.fromString(message.getEntities().get(0).getText());
        switch (command) {
            case CHANGE_INFO:
                user.changeUserStatus(ModuleEnum.CHANGE_INFO_MODULE.name());
                userService.saveUser(user);
                return generateMessageForInfoChanging(user);
            case START:
                return allModules.get(ModuleEnum.REGISTRATION_MODULE).executeMessage(update);
            default:
                return generateMessageForInfoChanging(user);

        }
    }

    private SendMessage generateMessageForInfoChanging(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        String text = EmojiParser.parseToUnicode("Напишите пожалуйста свое ФИО/Имя/Кличку/Погоняло, а я его запомню :new_moon_with_face:");
        sendMessage.setText(text);
        return sendMessage;
    }

    @Override
    public HandlerEnum getHandlerType() {
        return HandlerEnum.BOT_COMMAND_HANDLER;
    }
}
