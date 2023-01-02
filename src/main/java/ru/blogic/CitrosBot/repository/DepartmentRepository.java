package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.Department;

import java.util.Optional;

/**
 * Репозиторий для сущности отделов
 *
 * @author eyakimov
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    /**
     * Метод получения отдела по названию
     * @param departmentName - название отдела
     * @return - отдел
     */
    Optional<Department> findByNameOfDepartment(String departmentName);
}

