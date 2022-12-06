package ru.blogic.CitrosBot.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Интерфейс сервиса для работы с пользователем
 *
 * @author eyakimov
 */
public interface UserService {
    /**
     * Метод получения требуемого модуля в зависимости от состояния пользователя
     *
     * @param message - chat_id пользователя
     * @return ModuleEnum - вид модуля
     */
    ModuleEnum getModuleByUserState(Message message);
}
