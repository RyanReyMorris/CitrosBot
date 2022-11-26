package ru.blogic.CitrosBot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.blogic.CitrosBot.facade.TelegramFacade;
import ru.blogic.CitrosBot.service.TelegramBot;

@Configuration
public class SpringAppConfig {
    private final BotConfig botConfig;

    public SpringAppConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getBotPath()).build();
    }

    @Bean
    public TelegramBot springWebhookBot(SetWebhook setWebhook, TelegramFacade telegramFacade) {
        TelegramBot bot = new TelegramBot(telegramFacade, setWebhook);
        bot.setBotToken(botConfig.getBotToken());
        bot.setBotName(botConfig.getBotName());
        bot.setBotPath(botConfig.getBotPath());
        return bot;
    }
}
