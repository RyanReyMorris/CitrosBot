package ru.blogic.CitrosBot.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигураця бота
 *
 * @author eyakimov
 */
@Configuration
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {

    @Value("${bot.webhook-path}")
    String botPath;

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String botToken;

}
