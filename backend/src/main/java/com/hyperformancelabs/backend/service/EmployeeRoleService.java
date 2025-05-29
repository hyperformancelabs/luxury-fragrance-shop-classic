package com.hyperformancelabs.backend.service;

public interface EmployeeRoleService {
    // Thêm role cho nhân viên
    void addEmployeeRole(Integer employeeId, String roleName);

    // Xóa role của nhân viên
    void deleteEmployeeRoleByEmployeeId(Integer employeeId);
}
