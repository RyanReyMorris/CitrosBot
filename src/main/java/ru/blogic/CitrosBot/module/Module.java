package ru.blogic.CitrosBot.module;

import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Интерфейс модулей чат-бота
 *
 * @author eyakimov
 */
public interface Module {
    /**
     * Метод получения типа модуля
     *
     * @return ModuleEnum - тип модуля
     */
    ModuleEnum getModuleType();
}
