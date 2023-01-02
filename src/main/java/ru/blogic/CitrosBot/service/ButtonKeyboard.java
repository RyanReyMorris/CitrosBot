package ru.blogic.CitrosBot.service;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс, предназначенный для создания кнопок к сообщению. Необходим для облегчения логики создания кнопок
 *
 * @author eyakimov
 */
@ToString
@RequiredArgsConstructor
public class ButtonKeyboard {
    /**
     * Мапа кнопок для каждой строки, где ключ - номер строки, значение - мапа кнопок (callbackData<-->название)
     */
    private final Map<Integer, Map<String, String>> mapOfMessageButtons = new HashMap<>();

    /**
     * Мапа списков названий кнопок для каждой строки, где ключ - номер строки, значение - список названий для кнопок
     */
    private final Map<Integer, List<String>> mapOfMenuButtons = new HashMap<>();

    /**
     * Метод добавления кнопки в определенную строку.
     *
     * @param row          - строка
     * @param callBackData - дата кнопки
     * @param buttonName   - название кнопки.
     */
    public void addMessageButton(Integer row, String callBackData, String buttonName) {
        Map<String, String> oneRowButtons = new HashMap<>();
        oneRowButtons.put(callBackData, buttonName);
        if (mapOfMessageButtons.get(row).isEmpty()) {
            mapOfMessageButtons.put(row, oneRowButtons);
        } else {
            Map<String, String> rowOfButtons = mapOfMessageButtons.get(row);
            rowOfButtons.put(callBackData, buttonName);
            mapOfMessageButtons.put(row, rowOfButtons);
        }
    }

    /**
     * Метод получения объекта кнопок InlineKeyboardMarkup
     *
     * @return InlineKeyboardMarkup
     */
    public InlineKeyboardMarkup getMessageButtons() {
        if (mapOfMessageButtons.isEmpty()) {
            return new InlineKeyboardMarkup();
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Integer row : mapOfMessageButtons.keySet()) {
            List<InlineKeyboardButton> keyboardButtonsRow = getOneLineOfButtons(mapOfMessageButtons.get(row));
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    /**
     * Метод добавления кнопки меню
     *
     * @param row        - строка
     * @param buttonName - название кнопки
     */
    public void addMenuButton(Integer row, String buttonName) {
        List<String> oneRowButtons = new ArrayList<>();
        oneRowButtons.add(buttonName);
        if (mapOfMenuButtons.get(row).isEmpty()) {
            mapOfMenuButtons.put(row, oneRowButtons);
        } else {
            List<String> rowOfButtons = mapOfMenuButtons.get(row);
            rowOfButtons.add(buttonName);
            mapOfMenuButtons.put(row, rowOfButtons);
        }
    }

    /**
     * Метод получения кнопок меню ReplyKeyboardMarkup
     *
     * @return ReplyKeyboardMarkup
     */
    public ReplyKeyboardMarkup getMenuButtons() {
        if (mapOfMenuButtons.isEmpty()) {
            return new ReplyKeyboardMarkup();
        }
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (Integer row : mapOfMenuButtons.keySet()) {
            KeyboardRow keyboardButtons = getOneLineOfMenuButtons(mapOfMenuButtons.get(row));
            keyboardRows.add(keyboardButtons);
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
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
