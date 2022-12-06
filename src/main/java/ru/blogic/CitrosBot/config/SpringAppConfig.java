package ru.blogic.CitrosBot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Конфигураця прложения
 *
 * @author eyakimov
 */
@Configuration
public class SpringAppConfig {

    @Autowired
    private BotConfig botConfig;

    @Lazy
    @Autowired
    @Qualifier("visibleModules")
    private List<Module> visibleModules;

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
