package ru.blogic.CitrosBot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.blogic.CitrosBot.TelegramBot;

/**
 * Конфигурация приложения
 *
 * @author eyakimov
 */
@Configuration
public class SpringAppConfig {

    @Autowired
    private BotConfig botConfig;

    //методы ниже нужны для работы бота через веб-хук
    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getBotPath()).build();
    }

    @Bean
    public TelegramBot springWebhookBot(SetWebhook setWebhook) {
        TelegramBot bot = new TelegramBot(setWebhook);
        bot.setBotToken(botConfig.getBotToken());
        bot.setBotName(botConfig.getBotName());
        bot.setBotPath(botConfig.getBotPath());
        return bot;
    }
}
