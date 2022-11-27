package ru.blogic.CitrosBot.facade;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.handler.Handler;

import java.util.Map;

/**
 * Фасад сервис телеграм бота. Все данные поступающие от пользователя обрабатываются здесь.
 *
 * @author eyakimov
 */
@Component
public class TelegramFacade {

    @Resource()
    private Map<HandlerEnum, Handler> systemHandlers;

    public BotApiMethod<?> handleUpdate(Update update) {
        HandlerEnum updateType = update.hasCallbackQuery() ? HandlerEnum.CALL_BACK_QUERY_HANDLER : HandlerEnum.MESSAGE_HANDLER;
        return systemHandlers.get(updateType).handle(update);
    }
}
