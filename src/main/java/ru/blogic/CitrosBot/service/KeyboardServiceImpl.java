package ru.blogic.CitrosBot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервис создания кнопок
 *
 * @author eyakimov
 */
@Service
public class KeyboardServiceImpl {
    /**
     * Метод получения кнопок в одну строку
     *
     * @param mapOfButtons -  - мапа кнопок, где ключ - callbackData, значение - название для отображения
     * @return - кнопки
     */
    public InlineKeyboardMarkup getInlineButtons(Map<String, String> mapOfButtons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = getOneLineOfButtons(mapOfButtons);
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardButtonsRow));
        return inlineKeyboardMarkup;
    }

    /**
     * Метод получения многострочных кнопок
     *
     * @param mapOfButtons - мапа списков названий кнопок для каждой строки
     * @return - кнопки
     */
    public InlineKeyboardMarkup getMultilineButtons(Map<Integer, Map<String, String>> mapOfButtons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        Set<Integer> rowsCount = mapOfButtons.keySet();
        for (Integer row : rowsCount) {
            List<InlineKeyboardButton> keyboardButtonsRow = getOneLineOfButtons(mapOfButtons.get(row));
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    /**
     * Приватный универсальный метод создания списка кнопок
     *
     * @param mapOfButtons - мапа кнопок, где ключ - callbackData, значение - название для отображения
     * @return - список кнопок для одной строки
     */
    private List<InlineKeyboardButton> getOneLineOfButtons(Map<String, String> mapOfButtons) {
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        for (String buttonCallbackData : mapOfButtons.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(mapOfButtons.get(buttonCallbackData));
            button.setCallbackData(buttonCallbackData);
            keyboardButtonsRow.add(button);
        }
        return keyboardButtonsRow;
    }
}
