package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.blogic.CitrosBot.entity.UserEntity;

import java.time.LocalDate;
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
    @Query(value = "select * from user_entity where EXTRACT(MONTH FROM birthday) = EXTRACT(MONTH FROM cast(:birthday as date)) AND EXTRACT(DAY FROM birthday) = EXTRACT(DAY FROM cast(:birthday as date)) AND is_birthday_module_on = true", nativeQuery = true)
    List<UserEntity> findByBirthdayAndIsBirthdayModuleOnIsTrue(@Param("birthday") LocalDate birthday);

    /**
     * Метод получения списка всех НЕименинников
     *
     * @param birthday - передаваемая дата рождения
     * @return список пользователей
     */
    @Query(value = "select * from user_entity where (EXTRACT(MONTH FROM birthday) != EXTRACT(MONTH FROM cast(:birthday as date)) OR EXTRACT(DAY FROM birthday) != EXTRACT(DAY FROM cast(:birthday as date))) AND is_birthday_module_on = true", nativeQuery = true)
    List<UserEntity> findByBirthdayIsNotAndIsBirthdayModuleOnIsTrue(@Param("birthday") LocalDate birthday);

    @Query(value = "select * from user_entity u where exists (select 1 from anecdote a where a.author_id = u.id)", nativeQuery = true)
    List<UserEntity> findUsersWithAnecdotes();
}