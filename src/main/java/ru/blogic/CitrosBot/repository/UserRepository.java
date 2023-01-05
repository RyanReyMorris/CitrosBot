package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<UserEntity> findByBirthdayAndIsBirthdayModuleOnIsTrue(Date birthday);

    /**
     * Метод получения списка всех НЕименинников
     *
     * @param birthday - передаваемая дата рождения
     * @return список пользователей
     */
    List<UserEntity> findByBirthdayIsNotAndIsBirthdayModuleOnIsTrue(Date birthday);

    @Query(value = "select * from user_entity u where exists (select 1 from anecdote a where a.author_id = u.id)",  nativeQuery = true)
    List<UserEntity> findUsersWithAnecdotes();
}