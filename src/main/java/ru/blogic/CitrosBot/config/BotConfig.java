package ru.blogic.CitrosBot.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация бота
 *
 * @author eyakimov
 */
@Configuration
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {
    /**
     * Адрес веб-хука. Переписать при деплое
     */
    @Value("${bot.webhook-path}")
    String botPath;
    /**
     * Имя бота
     */
    @Value("${bot.name}")
    String botName;
    /**
     * Токен бота. За получением нового токена обращаться к - eyakimov
     */
    @Value("${bot.token}")
    String botToken;
}
