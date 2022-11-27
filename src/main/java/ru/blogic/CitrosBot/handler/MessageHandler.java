package ru.blogic.CitrosBot.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;

import java.util.Map;

/**
 * Хэндлер сообщений
 *
 * @author eyakimov
 */
@Component
public class MessageHandler implements Handler {

    @Resource()
    private Map<ModuleEnum, Module> systemModules;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        return systemModules.get(ModuleEnum.REGISTRATION_MODULE).execute(update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerEnum getHandlerType() {
        return HandlerEnum.MESSAGE_HANDLER;
    }
}
