package ru.blogic.CitrosBot.enums;

/**
 * Перечисление возможных системных хэндлеров
 *
 * @author eyakimov
 */
public enum HandlerEnum {
    /**
     * Хэндлер текстовых сообщений
     */
    MESSAGE_HANDLER,
    /**
     * Хэндлер кнопок (не считая кнопок главного меню)
     */
    CALL_BACK_QUERY_HANDLER,
    /**
     * Хэндел команд бота типа "/info"
     */
    BOT_COMMAND_HANDLER
}
