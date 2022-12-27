package ru.blogic.CitrosBot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Entity(name = "Department")
@Table(name = "department")
@Getter
@ToString
@RequiredArgsConstructor
public class Department {
    @Id
    @Column(name = "id")
    private Long id;

    @Id
    @Column(name = "name_of_department")
    private String nameOfDepartment;

    public static Department.DepartmentBuilder newBuilder() {
        return new Department().new DepartmentBuilder();
    }

    public class DepartmentBuilder {

        private DepartmentBuilder() {
        }

        public Department.DepartmentBuilder setId(Long id) {
            Department.this.id = id;
            return this;
        }

        public Department.DepartmentBuilder setName(String nameOfDepartment) {
            Department.this.nameOfDepartment = nameOfDepartment;
            return this;
        }

        public Department build() {
            return Department.this;
        }
    }
}
