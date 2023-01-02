package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.blogic.CitrosBot.entity.ServiceCallRequest;
import ru.blogic.CitrosBot.repository.ServiceCallRequestRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с заявками в техподдержку
 *
 * @author eyakimov
 */
@Transactional
@Service
public class ServiceCallRequestServiceImpl implements ServiceCallRequestService {

    @Autowired
    private ServiceCallRequestRepository serviceCallRequestRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllServiceCallRequestsByUser(Long userId) {
        serviceCallRequestRepository.deleteAllByFromUser_Id(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteServiceCallRequestById(Long serviceCallRequestId) {
        serviceCallRequestRepository.deleteById(serviceCallRequestId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceCallRequest saveServiceCallRequest(ServiceCallRequest serviceCallRequest) {
        return serviceCallRequestRepository.save(serviceCallRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceCallRequest> findServiceCallRequestById(Long id) {
        return serviceCallRequestRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceCallRequest> findAllServiceCallRequest() {
        return serviceCallRequestRepository.findAll();
    }
}
