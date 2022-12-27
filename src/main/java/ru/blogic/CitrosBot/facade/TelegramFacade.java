package ru.blogic.CitrosBot.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.handler.Handler;
import ru.blogic.CitrosBot.service.UserService;

import java.util.Map;

/**
 * Фасад сервис телеграм бота. Все данные поступающие от пользователя обрабатываются здесь.
 *
 * @author eyakimov
 */
@Component
public class TelegramFacade {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("systemHandlers")
    private Map<HandlerEnum, Handler> systemHandlers;

    public BotApiMethod<?> handleUpdate(Update update) {
        checkIsNewUser(update.getMessage());
        HandlerEnum updateType = getHandlerType(update);
        return systemHandlers.get(updateType).handle(update);
    }

    /**
     * Метод проверки пользователя на наличие в БД. Если пользователь новый - создаем о нем запись в бд.
     *
     * @param message - передаваемое сообщение
     */
    private void checkIsNewUser(Message message) {
        boolean isNewUser = message != null && userService.checkIfUserIsNew(message.getChat().getId());
        if (isNewUser) {
            userService.createNewUser(message);
        }
    }

    /**
     * Метод получения требуемого хэндлера по передаваемому update
     *
     * @param update - действие пользователя
     * @return HandlerEnum - нужный тип хэндлера
     */
    private HandlerEnum getHandlerType(Update update) {
        if (update.hasCallbackQuery()) {
            return HandlerEnum.CALL_BACK_QUERY_HANDLER;
        }
        if (update.getMessage().hasEntities() && update.getMessage().getEntities().get(0).getType().equals("bot_command")) {
            return HandlerEnum.BOT_COMMAND_HANDLER;
        }
        return HandlerEnum.MESSAGE_HANDLER;
    }
}
