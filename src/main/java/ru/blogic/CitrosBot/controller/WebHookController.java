package ru.blogic.CitrosBot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.service.TelegramBot;

/**
 * Контроллер. Необходим, поскольку работаем через веб-хук
 *
 * @author eyakimov
 */
@RestController
public class WebHookController {

    @Autowired
    private TelegramBot telegramBot;

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
