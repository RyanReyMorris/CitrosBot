package ru.blogic.CitrosBot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * Интерфейс по созданию ответных сообщений пользователей
 *
 * @author eyakimov
 */
public interface MessageService {
    /**
     * Метод получения сообщения
     *
     * @param messageText - текст сообщения
     * @param chatId      - id чата
     * @return объект SendMessage сообщение
     */
    SendMessage getMessage(String messageText, Long chatId);

    /**
     * Метод получения сообщения с нижними кнопками
     *
     * @param messageText          - текст сообщения
     * @param chatId               - id чата
     * @param inlineKeyboardMarkup - кнопки
     * @return объект SendMessage сообщение
     */
    SendMessage getMessageWithButtons(String messageText, Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup);

    /**
     * Метод получения сообщения с кнопками главного меню
     *
     * @param messageText         - текст сообщения
     * @param chatId              - id чата
     * @param replyKeyboardMarkup - кнопки меню
     * @return объект SendMessage сообщение
     */
    SendMessage getMessageWithMenuButtons(String messageText, Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup);

    /**
     * Метод получения сообщения об ошибке
     *
     * @param chatId      - id чата
     * @return объект SendMessage сообщение
     */
    SendMessage getErrorMessage(Long chatId);
}
