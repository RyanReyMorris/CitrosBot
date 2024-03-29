package ru.blogic.CitrosBot.enums;

import java.util.Arrays;

/**
 * Перечисление основных доступных команд бота
 *
 * @author eyakimov
 */
public enum BotCommandEnum {
    /**
    * Команда старта
    */
    START("/start"),

    /**
     * Команда изменения персональной информации
     */
    CHANGE_INFO("/changeinfo"),

    /**
     * Команда подключения модуля "Дни рождения"
     */
    BIRTHDAY("/birthday"),

    /**
     * Команда модуля "Анекдоты"
     */
    ANECDOTE("/anecdote"),

    /**
     * Команда перехода в модуль техподдержки
     */
    SERVICE("/service"),

    /**
     * Секретная команда включения админских прав
     */
    ENABLE_ADMIN_ROOTS("/adminrootson"),

    /**
     * Секретная команда отключения админских прав
     */
    DISABLE_ADMIN_ROOTS("/adminrootsoff"),

    /**
     * Команда получения информации по боту
     */
    HELP("/help");

    private final String command;

    BotCommandEnum(String command) {
        this.command = command;
    }

    /**
     * Метод получения enam'a по текстовому представлению
     *
     * @param botCommand - текстовое представление
     * @return элемент BotCommandEnum
     * @throws IllegalArgumentException - в случае, если была передана не существующая команда
     */
    public static BotCommandEnum fromString(String botCommand) throws IllegalArgumentException {
        return Arrays.stream(BotCommandEnum.values())
                .filter(v -> v.command.equals(botCommand))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная команда: " + botCommand));
    }
}
