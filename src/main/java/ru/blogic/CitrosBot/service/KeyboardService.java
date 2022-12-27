package ru.blogic.CitrosBot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Map;

/**
 * Сервис для создания клавиатур сообщений и главного меню
 *
 * @author eyakimov
 */
public interface KeyboardService {
    /**
     * Метод получения кнопок меню
     * @param mapOfButtons - мапа списков названий кнопок для каждой строки, где ключ - номер строки, значение - список названий для кнопок
     * @return - кнопки меню
     */
    ReplyKeyboardMarkup getMenuButtons(Map<Integer, List<String>> mapOfButtons);

    /**
     * Метод получения кнопок сообщения в одну строку
     *
     * @param mapOfButtons - мапа кнопок, где ключ - callbackData, значение - название для отображения
     * @return - кнопки сообщения
     */
    InlineKeyboardMarkup getInlineButtons(Map<String, String> mapOfButtons);

    /**
     * Метод получения многострочных кнопок сообщения
     *
     * @param mapOfButtons - мапа списков названий кнопок для каждой строки, где ключ - номер строки, значение - мапа кнопок (callbackData<-->название)
     * @return - кнопки сообщения
     */
    InlineKeyboardMarkup getMultilineButtons(Map<Integer, Map<String, String>> mapOfButtons);

}
