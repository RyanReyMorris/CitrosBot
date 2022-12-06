package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.User;

/**
 * Репозиторий для сущности пользователя
 *
 * @author eyakimov
 */
public interface UserRepository extends JpaRepository<User, Long> {
}