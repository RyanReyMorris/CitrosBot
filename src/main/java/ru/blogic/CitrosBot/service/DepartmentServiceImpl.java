package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.blogic.CitrosBot.entity.Department;
import ru.blogic.CitrosBot.repository.DepartmentRepository;

import java.text.MessageFormat;
import java.util.List;

/**
 * Сервис для работы с отделами
 *
 * @author eyakimov
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistingDepartment(String departmentName) {
        return departmentRepository.findAll()
                .stream()
                .anyMatch(department -> department.getNameOfDepartment().equals(departmentName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Department getDepartmentByName(String departmentName) {
        return departmentRepository.findByNameOfDepartment(departmentName)
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Отдела с названием {0} не было найдено", departmentName)));
    }
}
