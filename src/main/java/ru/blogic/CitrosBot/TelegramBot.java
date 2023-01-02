package ru.blogic.CitrosBot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
     * Метод предназначенный для отправки сообщений пользователю
     *
     * @param sendMessage - передаваемое сообщение
     */
    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
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
