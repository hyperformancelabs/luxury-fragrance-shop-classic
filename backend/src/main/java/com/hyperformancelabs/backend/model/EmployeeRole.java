package com.hyperformancelabs.backend.model;

import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.model.Role;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "EmployeeRole", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_EmployeeRole", columnNames = {"employee_id", "role_id"})
})
@Data
public class EmployeeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_role_id")
    private Integer employeeRoleId;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
    private Role role;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    // Getters and Setters
}
