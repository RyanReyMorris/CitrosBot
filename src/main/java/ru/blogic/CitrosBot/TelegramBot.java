package ru.blogic.CitrosBot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.VideoNote;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.blogic.CitrosBot.facade.TelegramFacade;

/**
 * Телеграм бот. Отвечает за клиент-серверное взаимодействие
 *
 * @author eyakimov
 */
@Slf4j
@Getter
@Setter
public class TelegramBot extends SpringWebhookBot {
    /**
     * Путь веб-хука
     */
    private String botPath;
    /**
     * Имя бота
     */
    private String botName;
    /**
     * Токен бота
     */
    private String botToken;

    @Autowired
    private TelegramFacade telegramFacade;

    //Методы требуется переопределить для работы бота через веб-хук
    public TelegramBot(SetWebhook setWebhook) {
        super(setWebhook);
    }

    //Методы требуется переопределить для работы бота через веб-хук
    public TelegramBot(DefaultBotOptions options, SetWebhook setWebhook) {
        super(options, setWebhook);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return telegramFacade.handleUpdate(update);
    }

    /**
     * Метод предназначенный для отправки сообщения пользователю.
     *
     * @param sendObject - передаваемое сообщение с фото/видео/аудио или же обычное текстовое сообщение
     */
    public void sendMessageToUser(PartialBotApiMethod<Message> sendObject) {
        try {
            if (sendObject.getClass().equals(SendMessage.class)) {
                execute((SendMessage) sendObject);
            }
            if (sendObject.getClass().equals(SendVideoNote.class)) {
                execute((SendVideoNote) sendObject);
            }
            if (sendObject.getClass().equals(SendPhoto.class)) {
                execute((SendPhoto) sendObject);
            }
            if (sendObject.getClass().equals(SendVoice.class)) {
                execute((SendVoice) sendObject);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод предназначенный для удаления сообщения
     *
     * @param message - передаваемыый объект удаляемого сообщения
     */
    public void deleteMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(message.getChatId());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBotUsername() {
        return getBotName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRegister() {
        super.onRegister();
    }

}
