package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.ServiceCallRequest;
/**
 * Репозиторий для сущности запросов в техподдержку
 *
 * @author eyakimov
 */
public interface ServiceCallRequestRepository extends JpaRepository<ServiceCallRequest, Long> {

    /**
     * Метод удаления всех заявок от конкретного пользователя
     */
    void deleteAllByFromUser_Id(Long fromUser);
}
