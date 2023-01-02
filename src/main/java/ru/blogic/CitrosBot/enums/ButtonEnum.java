package ru.blogic.CitrosBot.enums;

import java.util.List;

/**
 * Перечисление всех основных функциональных кнопок, использующихся в приложении.
 * CallBackData кнопки - не задается отдельным полем. Вместо этого используется название элемента перечисления.
 *
 * @author eyakimov
 */
public enum ButtonEnum {
    /**
     * Кнопка старта модуля изменения данных
     */
    START_CHANGE_INFO_MODULE("Давайте!"),

    /**
     * Кнопка изменения имени
     */
    CHANGE_INFO_NAME("Полное имя"),

    /**
     * Кнопка изменения часового пояса
     */
    CHANGE_INFO_TIME_ZONE("Часовой пояс"),

    /**
     * Кнопка изменения отдела
     */
    CHANGE_INFO_DEPARTMENT("Отдел"),

    /**
     * Кнопка изменения даты рождения
     */
    CHANGE_INFO_BIRTHDAY("Дата рождения"),

    /**
     * Кнопка выхода из диалога (модуля)
     */
    EXIT_MODULE("-Выйти-"),

    /**
     * Кнопка блокировки пользователя
     */
    BLOCK_USER("Заблокировать пользователя"),

    /**
     * Кнопка разблокировки пользователя
     */
    UNBLOCK_USER("Разблокировать пользователя"),

    /**
     * Ответить пользователю
     */
    REPLY_USER("Ответить пользователю"),

    /**
     * Кнопка выхода из диалога (модуля)
     */
    START_MAIN_MENU_MODULE("Начать!");

    /**
     * Название кнопки
     */
    private final String buttonName;

    ButtonEnum(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public static List<ButtonEnum> getUserInfoButtons() {
        return List.of(CHANGE_INFO_NAME, CHANGE_INFO_DEPARTMENT, CHANGE_INFO_BIRTHDAY, CHANGE_INFO_TIME_ZONE);
    }
}
