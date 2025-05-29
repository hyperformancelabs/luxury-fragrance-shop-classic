package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Integer> {

    // Xóa role của nhân viên
    void deleteByEmployee_EmployeeId(Integer employeeId);
}
