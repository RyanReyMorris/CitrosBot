package ru.blogic.CitrosBot.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Интерфейс сервиса для работы с пользователем
 *
 * @author eyakimov
 */
public interface UserService {
    /**
     * Метод сохранения пользователя в базу данных
     *
     * @param user - передаваемый пользователь
     */
    void saveUser(User user);

    /**
     * Получение пользователя по его id
     *
     * @param id - передаваемый id пользователя
     * @return - объект пользователя
     */
    User findUserById(Long id);

    /**
     * Метод создания нового пользователя
     *
     * @param message - передаваемое сообщение
     * @return - объект пользователя
     */
    User createNewUser(Message message);

    /**
     * Метод проверки, имеется ли пользователь с передаваемым id в базе данных
     *
     * @param id пользователя
     * @return - boolean: true, если пользователя нет в бд, иначе - false
     */
    boolean checkIfUserIsNew(Long id);

    /**
     * Метод получения требуемого модуля в зависимости от состояния пользователя
     *
     * @param id - передавваемый id пользователя
     * @return ModuleEnum - вид модуля
     */
    ModuleEnum getModuleByUserState(Long id);
}
