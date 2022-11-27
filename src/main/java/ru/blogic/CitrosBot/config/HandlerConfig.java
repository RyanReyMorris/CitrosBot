package ru.blogic.CitrosBot.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.blogic.CitrosBot.enums.HandlerEnum;
import ru.blogic.CitrosBot.handler.Handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Конфигурация хэндлеров для обработки информации от клиента
 *
 * @author eyakimov
 */
@Configuration
public class HandlerConfig {

    @Autowired
    private List<Handler> handlers;

    @Bean
    public Map<HandlerEnum, Handler> systemHandlers() {
        Map<HandlerEnum, Handler> systemHandlers = new HashMap<>();
        for (Handler handler : handlers) {
            systemHandlers.put(handler.getHandlerType(), handler);
        }
        return systemHandlers;
    }
}
