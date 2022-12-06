package ru.blogic.CitrosBot.handler;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;
import ru.blogic.CitrosBot.service.UserService;

import java.util.Map;

/**
 * Хэндлер кнопок
 *
 * @author eyakimov
 */
@Component
public class CallbackQueryHandler implements Handler {

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
        CallbackQuery callbackQuery = update.getCallbackQuery();
        ModuleEnum module = userService.getModuleByUserState(callbackQuery.getMessage());
        return allModules.get(module).executeCallbackQuery(update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerEnum getHandlerType() {
        return HandlerEnum.CALL_BACK_QUERY_HANDLER;
    }
}
