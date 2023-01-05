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
     * Удалить анекдот
     */
    DELETE_ANECDOTE("Удалить анекдот"),

    /**
     * Кнопка "Мои анекдоты"
     */
    MY_ANECDOTES("Мои анекдоты"),

    /**
     * Кнопка Вперед
     */
    REDO_ANECDOTES("Вперед->"),

    /**
     * Кнопка Назад
     */
    UNDO_ANECDOTES("<-Назад"),

    /**
     * Кнопка Вперед
     */
    REDO_USERS("Вперед->"),

    /**
     * Кнопка Назад
     */
    UNDO_USERS("<-Назад"),

    /**
     * Кнопка отклонения
     */
    NO("Нет"),

    /**
     * Кнопка согласия
     */
    YES("Да"),

    /**
     * Записать анекдот
     */
    GET_AUTHOR_ANECDOTES("Найти по автору"),

    /**
     * Записать анекдот
     */
    GET_RANDOM_ANECDOTE("Рандомная шутка"),

    /**
     * Записать анекдот
     */
    CREATE_ANECDOTE("Создать анекдот"),

    /**
     * Кнопка отключения модуля "Дни рождения"
     */
    DISABLE_BIRTHDAY_MODULE("Отключить"),

    /**
     * Кнопка подключения модуля "Дни рождения"
     */
    ENABLE_BIRTHDAY_MODULE("Подключить"),

    /**
     * Кнопка старта модуля изменения данных
     */
    START_CHANGE_INFO_MODULE("Давайте!"),

    /**
     * Кнопка старта модуля изменения данных
     */
    START_REGISTRATION_MODULE("Начать регистрацию!"),

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
