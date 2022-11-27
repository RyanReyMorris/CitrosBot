package ru.blogic.CitrosBot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.blogic.CitrosBot.facade.TelegramFacade;

/**
 * Телеграм бот. Отвечает за клиент-серверное взаимодействие
 *
 * @author eyakimov
 */
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
