package ru.blogic.CitrosBot.facade;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Фасад сервис телеграм бота. Все данные поступающие от пользователя обрабатываются здесь.
 *
 * @author eyakimov
 */
@Component
public class TelegramFacade {

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return null;
        } else {
            Message message = update.getMessage();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            if (message.hasText()) {
                sendMessage.setText("Hello world");
                return sendMessage;
            }
        }
        return null;
    }
}
