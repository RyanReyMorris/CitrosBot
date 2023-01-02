package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.Department;
import ru.blogic.CitrosBot.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для сущности пользователя
 *
 * @author eyakimov
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Метод получения списка всех админов
     * @return список пользователей
     */
    List<UserEntity> findAllByIsAdminIsTrue();
}