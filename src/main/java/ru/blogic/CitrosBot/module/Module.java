package ru.blogic.CitrosBot.module;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Интерфейс модулей чат-бота
 *
 * @author eyakimov
 */
public interface Module {
    /**
     * Метод обработки события
     *
     * @param update - данные от клиента
     * @return возвращаемые данные (сообщения/кнопки/меню и т.п.)
     */
    BotApiMethod<?> execute(Update update);

    /**
     * Метод получения типа модуля
     *
     * @return ModuleEnum - тип модуля
     */
    ModuleEnum getModuleType();
}
