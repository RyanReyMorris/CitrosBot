package ru.blogic.CitrosBot.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * Сервис по созданию сообщений
 *
 * @author eyakimov
 */
@Service
public class MessageServiceImpl implements MessageService {
    /**
     * Текст ошибки. Выводится в случае, если пользователь ввел неверную команду
     */
    private final String errorText = ":warning: Ошибка: данная команда недоступна в данным момент или же неизвестна";

    @Override
    public SendMessage getErrorMessage(Long chatId) {
        return createMessage(errorText, chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage getMessage(String messageText, Long chatId) {
        return createMessage(messageText, chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage getMessageWithButtons(String messageText, Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = createMessage(messageText, chatId);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage getMessageWithMenuButtons(String messageText, Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = createMessage(messageText, chatId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    /**
     * Универсальный приватный метод создания сообщения
     *
     * @param messageText - текст сообщения
     * @param chatId      - id чата
     * @return SendMessage
     */
    private SendMessage createMessage(String messageText, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(EmojiParser.parseToUnicode(messageText));
        return sendMessage;
    }
}
