package ru.blogic.CitrosBot.service;

import ru.blogic.CitrosBot.entity.Department;

import java.util.List;

/**
 * Интерфейс сервиса для работы с отделами
 *
 * @author eyakimov
 */
public interface DepartmentService {
    /**
     * Метод получения списка всех отделов
     *
     * @return список отделов
     */
    List<Department> getAllDepartments();

    /**
     * Имеется ли запись об отделе с данным названием в базе данных
     * @param departmentName - название отдела
     * @return - boolean: true, если запись имеется, иначе - false
     */
    boolean isExistingDepartment(String departmentName);

    /**
     * Метод получения отдела по его названию
     * @param departmentName - название отдела
     * @return - объект отдела
     */
    Department getDepartmentByName(String departmentName);
}
