package ru.blogic.CitrosBot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Сервис создания кнопок
 *
 * @author eyakimov
 */
@Service
public class KeyboardServiceImpl implements KeyboardService {
    /**
     * {@inheritDoc}
     */
    @Override
    public ReplyKeyboardMarkup getMenuButtons(Map<Integer, List<String>> mapOfButtons) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (Integer row : mapOfButtons.keySet()) {
            KeyboardRow keyboardButtons = getOneLineOfMenuButtons(mapOfButtons.get(row));
            keyboardRows.add(keyboardButtons);
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup getInlineButtons(Map<String, String> mapOfButtons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = getOneLineOfButtons(mapOfButtons);
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardButtonsRow));
        return inlineKeyboardMarkup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InlineKeyboardMarkup getMultilineButtons(Map<Integer, Map<String, String>> mapOfButtons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Integer row : mapOfButtons.keySet()) {
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


    /**
     * Приватный универсальный метод создания списка кнопок меню
     *
     * @param listOfButtons - список кнопок для одной строки
     * @return - список кнопок для одной строки меню
     */
    private KeyboardRow getOneLineOfMenuButtons(List<String> listOfButtons) {
        KeyboardRow keyboardButtons = new KeyboardRow();
        for (String button : listOfButtons) {
            keyboardButtons.add(button);
        }
        return keyboardButtons;
    }
}
