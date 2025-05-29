package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.EmployeeAdminDisplayDTO;
import com.hyperformancelabs.backend.dto.EmployeeDTO;
import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public EmployeeDTO getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee != null) {
            return convertToEmployeeDTO(employee);
        }
        return null;
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeAdminDisplayDTO employee) {
        if (employee.getDateOfBirth() != null && employee.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ. Vui lòng chọn ngày trong quá khứ.");
        }

        Employee newEmployee = new Employee();
        newEmployee.setUsername(employee.getUsername());
        newEmployee.setPassword(employee.getPassword());
        newEmployee.setFullName(employee.getFullName());
        newEmployee.setPhoneNumber(employee.getPhoneNumber());
        newEmployee.setEmail(employee.getEmail());
        newEmployee.setAddress(employee.getAddress());
        newEmployee.setStatus(employee.getStatus());
        newEmployee.setStartDate(Date.valueOf(employee.getStartDate()));

        if (employee.getDateOfBirth() != null) {
            newEmployee.setDateOfBirth(Date.valueOf(employee.getDateOfBirth()));
        }

        newEmployee.setProfilePictureUrl(employee.getProfilePictureUrl());

        employeeRepository.save(newEmployee);
        return convertToEmployeeDTO(newEmployee);
    }

    @Override
    public void updateEmployee(EmployeeDTO dto) {
        Employee entity = employeeRepository.findByEmployeeId(dto.getEmployeeId());
        if (entity == null) {
            return;
        }

        entity.setFullName(dto.getFullName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setStatus(dto.getStatus());
        entity.setProfilePictureUrl(dto.getProfilePictureUrl());

        if (dto.getDateOfBirth() != null) {
            entity.setDateOfBirth(new java.sql.Date(dto.getDateOfBirth().getTime()));
        }

        if (dto.getStartDate() != null) {
            entity.setStartDate(new java.sql.Date(dto.getStartDate().getTime()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(dto.getPassword());
        }

        if (dto.getLastLogin() != null) {
            entity.setLastLogin(new java.sql.Timestamp(dto.getLastLogin().getTime()));
        }

        employeeRepository.save(entity);
    }

    @Override
    public void deleteEmployee(Integer employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    @Override
    public EmployeeDTO findActiveSystemAdminByEmailOrPhone(String emailOrPhone) {
        Employee employee = employeeRepository.findActiveSystemAdminByEmailOrPhone(emailOrPhone);
        return employee != null ? convertToEmployeeDTO(employee) : null;
    }

    @Override
    public EmployeeDTO getEmployeeByUsername(String username) {
        return employeeRepository.findByUsername(username)
                .map(this::convertToEmployeeDTO)
                .orElse(null);
    }

    @Override
    public List<String> findActiveRoleNamesByEmployeeId(Integer employeeId) {
        return employeeRepository.findActiveRoleNamesByEmployeeId(employeeId);
    }

    @Override
    public Page<EmployeeAdminDisplayDTO> findEmployeesWithFilters(String status, String roleName, String keyword, String sortField, String sortDir, Pageable pageable) {
        return employeeRepository.findEmployeesWithFilters(status, roleName, keyword, sortField, sortDir, pageable)
                .map(this::convertToEmployeeAdminDisplayDTO);
    }

    private EmployeeAdminDisplayDTO convertToEmployeeAdminDisplayDTO(Object[] result) {
        return new EmployeeAdminDisplayDTO(
                (Integer) result[0],                        // employeeId
                (String) result[1],                         // username
                (String) result[2],                         // fullName
                (String) result[3],                        // password
                (String) result[4],                         // phoneNumber
                (String) result[5],                         // email
                (String) result[6],                         // address
                (String) result[7],                         // status
                result[8] != null ? ((Date) result[8]).toLocalDate() : null,
                result[9] != null ? ((Date) result[9]).toLocalDate() : null,
                (String) result[10],                         // profilePictureUrl
                (String) result[11]                         // roles
        );
    }


    private EmployeeDTO convertToEmployeeDTO(Employee employee) {
        return new EmployeeDTO(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getPassword(),
                employee.getFullName(),
                employee.getPhoneNumber(),
                employee.getEmail(),
                employee.getAddress(),
                employee.getStatus(),
                employee.getStartDate(),
                employee.getLastLogin(),
                employee.getDateOfBirth(),
                employee.getProfilePictureUrl()
        );
    }
}
