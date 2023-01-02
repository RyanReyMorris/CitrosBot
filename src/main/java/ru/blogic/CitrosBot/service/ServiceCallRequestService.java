package ru.blogic.CitrosBot.service;

import ru.blogic.CitrosBot.entity.ServiceCallRequest;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с заявками в техподдержку
 *
 * @author eyakimov
 */
public interface ServiceCallRequestService {

    /**
     * Метод удаления всех заявок для конкретного пользователя
     *
     * @param userId - передаваемый id пользователя
     */
    void deleteAllServiceCallRequestsByUser(Long userId);

    /**
     * Метод удаления заявки из базы данных по id
     *
     * @param serviceCallRequestId - id передаваемой заявки в техподдержку
     */
    void deleteServiceCallRequestById(Long serviceCallRequestId);

    /**
     * Метод сохранения заявки в базу данных
     *
     * @param serviceCallRequest - передаваемая заявка в техподдержку
     */
    ServiceCallRequest saveServiceCallRequest(ServiceCallRequest serviceCallRequest);

    /**
     * Получение заявки по её id
     *
     * @param id - передаваемый id заявки
     * @return - объект заявки
     */
    Optional<ServiceCallRequest> findServiceCallRequestById(Long id);

    /**
     * Получение списка всех заявок
     *
     * @return - объект заявки
     */
    List<ServiceCallRequest> findAllServiceCallRequest();
}
