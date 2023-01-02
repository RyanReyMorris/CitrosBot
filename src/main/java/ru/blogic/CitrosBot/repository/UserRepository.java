package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.UserEntity;

import java.util.Date;
import java.util.List;

/**
 * Репозиторий для сущности пользователя
 *
 * @author eyakimov
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Метод получения списка всех админов
     *
     * @return список пользователей
     */
    List<UserEntity> findAllByIsAdminIsTrue();

    /**
     * Метод получения списка всех именинников
     *
     * @param birthday - передаваемая дата рождения
     * @return список пользователей
     */
    List<UserEntity> findAllByBirthdayAndIsBirthdayModuleOnIsTrue(Date birthday);

    /**
     * Метод получения списка всех НЕименинников
     *
     * @param birthday - передаваемая дата рождения
     * @return список пользователей
     */
    List<UserEntity> findAllByBirthdayNotAndIsBirthdayModuleOnIsTrue(Date birthday);
}