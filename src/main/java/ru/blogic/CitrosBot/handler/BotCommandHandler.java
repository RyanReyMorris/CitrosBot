package ru.blogic.CitrosBot.handler;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.BotCommandEnum;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;
import ru.blogic.CitrosBot.service.KeyboardService;
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
    private KeyboardService keyboardService;

    @Autowired
    @Qualifier("allModules")
    private Map<ModuleEnum, Module> allModules;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        UserEntity userEntity = userService.findUserById(update.getMessage().getChat().getId());
        BotCommandEnum command = BotCommandEnum.fromString(message.getEntities().get(0).getText());
        switch (command) {
            case HELP:
                telegramBot.deleteMessage(message);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                return generateHelpMessage(userEntity);
            case SERVICE:
                telegramBot.deleteMessage(message);
                userEntity.changeActiveModule(ModuleEnum.SERVICE_MODULE.name());
                userService.saveUser(userEntity);
                return allModules.get(ModuleEnum.SERVICE_MODULE).executeMessage(update);
            case CHANGE_INFO:
                telegramBot.deleteMessage(message);
                if (!userEntity.isRegistered()) {
                    return generateUnknownCommandMessage(userEntity);
                }
                userEntity.changeActiveModule(ModuleEnum.CHANGE_INFO_MODULE.name());
                userEntity.changeUserInfoStatus(ButtonEnum.START_CHANGE_INFO_MODULE.name());
                userService.saveUser(userEntity);
                return allModules.get(ModuleEnum.CHANGE_INFO_MODULE).executeMessage(update);
            case START:
                telegramBot.deleteMessage(message);
                if (userEntity.getActiveModule().equals(ModuleEnum.REGISTRATION_MODULE.name())) {
                    return allModules.get(ModuleEnum.REGISTRATION_MODULE).executeMessage(update);
                }
                return allModules.get(ModuleEnum.MAIN_MENU_MODULE).executeMessage(update);
            default:
                return generateUnknownCommandMessage(userEntity);

        }
    }

    private SendMessage generateUnknownCommandMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = EmojiParser.parseToUnicode(":warning: Ошибка: данная команда недоступна в данным момент или же неизвестна");
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage generateHelpMessage(UserEntity userEntity) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userEntity.getChatId());
        String text = MessageFormat.format(
                ":iphone: Информация по CitrosBot:{0}" +
                        ":gear: Я - небольшой творческий проект с расширяемой системой модулей  {1}" +
                        ":page_with_curl: В главном меню перечислен список всех доступных модулей {2}" +
                        ":envelope: Вы можете принять участие в моей разработке, оставить обо мне отзыв или предложить новый функционал. Для этого используйте команду /service {3}" +
                        ":bust_in_silhouette: Если хотите изменить данные о себе, используйте - /changeinfo {4}" +
                        ":information_source: Если вдруг что-то забудете, всегда можете написать мне /help"
                , "\n", "\n", "\n", "\n", "\n");
        text = EmojiParser.parseToUnicode(text);
        sendMessage.setText(text);
        return sendMessage;
    }

    @Override
    public HandlerEnum getHandlerType() {
        return HandlerEnum.BOT_COMMAND_HANDLER;
    }
}
