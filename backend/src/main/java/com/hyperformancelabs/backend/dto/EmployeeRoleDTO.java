package com.hyperformancelabs.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRoleDTO {

    private Integer employeeRoleId;

    private Integer employeeId; // Chỉ cần ID của employee

    private Integer roleId; // Chỉ cần ID của role

    private String status;
}
