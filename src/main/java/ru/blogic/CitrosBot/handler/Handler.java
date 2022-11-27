package ru.blogic.CitrosBot.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.HandlerEnum;

/**
 * Интерфейс хэндлера для обрабоки основных типов запросов
 *
 * @author eyakimov
 */
public interface Handler {
    /**
     * Метод обработки апдейта
     *
     * @param update - данные от клиента
     * @return возвращаемые данные (сообщения/кнопки/меню)
     */
    BotApiMethod<?> handle(Update update);

    /**
     * Метод получения типа хэнлера
     *
     * @return HandlerEnum - вид хэнлера
     */
    HandlerEnum getHandlerType();
}
