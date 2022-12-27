package ru.blogic.CitrosBot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;
import ru.blogic.CitrosBot.service.UserService;

import java.util.Map;

/**
 * Хэндлер сообщений
 *
 * @author eyakimov
 */
@Component
public class MessageHandler implements Handler {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("allModules")
    private Map<ModuleEnum, Module> allModules;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        ModuleEnum module = userService.getModuleByUserState(message.getChat().getId());
        return allModules.get(module).executeMessage(update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerEnum getHandlerType() {
        return HandlerEnum.MESSAGE_HANDLER;
    }
}
