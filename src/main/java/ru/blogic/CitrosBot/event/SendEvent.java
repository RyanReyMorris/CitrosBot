package ru.blogic.CitrosBot.event;

import lombok.Setter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.config.ApplicationContextProvider;

/**
 * Класс-обертка для отправляемого события
 *
 * @author eyakimov
 */
@Setter
public class SendEvent extends Thread {
    /**
     * Отправляемое сообщение
     */
    private SendMessage sendMessage;

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void run() {
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        telegramBot.sendMessage(sendMessage);
    }
}
