package ru.blogic.CitrosBot.module;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.ModuleEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Интерфейс модулей чат-бота
 *
 * @author eyakimov
 */
public interface Module {
    /**
     * Метод обработки события сообщения
     *
     * @param update - данные от клиента
     * @return возвращаемые данные (сообщения/кнопки/меню и т.п.)
     */
    BotApiMethod<?> executeMessage(Update update);

    /**
     * Метод обработки события нажатия кнопки
     *
     * @param update - данные от клиента
     * @return возвращаемые данные (сообщения/кнопки/меню и т.п.)
     */
    BotApiMethod<?> executeCallbackQuery(Update update);

    /**
     * Метод получения типа модуля
     *
     * @return ModuleEnum - тип модуля
     */
    ModuleEnum getModuleType();
}
