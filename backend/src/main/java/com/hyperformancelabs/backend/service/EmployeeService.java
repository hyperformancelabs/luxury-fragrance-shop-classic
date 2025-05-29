package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
//    void registerEmployee(EmployeeRegisterRequest request);
//    LoginResponse login(EmployeeLoginRequest request);
//    EmployeeProfileResponse getEmployeeProfile(String username);
//    EmployeeProfileResponse updateEmployeeProfile(String username, EmployeeUpdateRequest request);

    // Tìm nhân viên theo id
    EmployeeDTO getEmployeeById(Integer employeeId);

    // Thêm nhân viên
    EmployeeDTO addEmployee(EmployeeAdminDisplayDTO employee);

    // Cập nhật nhân viên
    void updateEmployee(EmployeeDTO employee);

    // Xóa nhân viên
    void deleteEmployee(Integer employeeId);

    // Lấy system admin theo email hoặc số điện thoại
    EmployeeDTO findActiveSystemAdminByEmailOrPhone(String emailOrPhone);

    // Lấy thông tin nhân viên theo username
    EmployeeDTO getEmployeeByUsername(String username);

    // Lấy role của nhân viên theo id
    List<String> findActiveRoleNamesByEmployeeId(Integer employeeId);

    // Lọc nhân viên
    Page<EmployeeAdminDisplayDTO> findEmployeesWithFilters(String status, String roleName, String keyword, String sortField, String sortDir, Pageable pageable);
}
