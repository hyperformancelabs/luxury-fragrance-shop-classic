package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.admin.common.EmployeeDTO;
import com.hyperformancelabs.backend.dto.admin.response.EmployeeAdminDisplayDTO;
// For commented out code, if uncommented:
// import com.hyperformancelabs.backend.dto.admin.request.EmployeeRegisterRequest;
// import com.hyperformancelabs.backend.dto.admin.response.LoginResponse;
// import com.hyperformancelabs.backend.dto.admin.response.EmployeeProfileResponse;
// import com.hyperformancelabs.backend.dto.admin.request.EmployeeUpdateRequest;

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

    // Cập nhật nhân viên với kết quả boolean
    boolean updateEmployeeProfile(EmployeeDTO employee);

    // Xóa nhân viên
    void deleteEmployee(Integer employeeId);

    // Lấy system admin theo username, email hoặc số điện thoại
    EmployeeDTO findActiveSystemAdminByUsernameEmailOrPhone(String usernameEmailOrPhone);

    // Lấy thông tin nhân viên theo username
    EmployeeDTO getEmployeeByUsername(String username);

    // Lấy role của nhân viên theo id
    List<String> findActiveRoleNamesByEmployeeId(Integer employeeId);

    // Lọc nhân viên
    Page<EmployeeAdminDisplayDTO> findEmployeesWithFilters(String status, String roleName, String keyword, String sortField, String sortDir, Pageable pageable);
}
