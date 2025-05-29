package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.dto.EmployeeDTO;
import com.hyperformancelabs.backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    // Tìm nhân viên theo username
    Optional<Employee> findByUsername(String username);

    // Tìm nhân viên theo id
    Employee findByEmployeeId(Integer employeeId);

    @Query(value = """
    SELECT e.*
    FROM Employee e
    JOIN EmployeeRole er ON e.employee_id = er.employee_id
    JOIN Role r ON er.role_id = r.role_id
    JOIN RolePermission rp ON r.role_id = rp.role_id
    JOIN Permission p ON rp.permission_id = p.permission_id
    WHERE 
        (:emailOrPhone IS NOT NULL AND (e.email = :emailOrPhone OR e.phone_number = :emailOrPhone))
        AND r.role_name IN ('System Admin', 'Order Staff', 'Material Staff')
        AND e.status = 'active'
        AND er.status = 'active'
        AND r.status = 'active'
    """, nativeQuery = true)
    Employee findActiveSystemAdminByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);

    // Lấy tên role của nhân viên theo id
    @Query("""
        SELECT r.roleName
        FROM EmployeeRole er
        JOIN er.role r
        WHERE er.employee.employeeId = :employeeId
          AND er.status = 'active'
          AND r.status = 'active'
    """)
    List<String> findActiveRoleNamesByEmployeeId(@Param("employeeId") Integer employeeId);

    // Lấy thông tin tất cả employee và role name
    @Query(
            value = """
        SELECT 
            e.employee_id,
            e.username,
            e.full_name,
            e.password,
            e.phone_number,
            e.email,
            e.address,
            e.status,
            e.start_date,
            e.date_of_birth,
            e.profile_picture_url,
            r.role_name
        FROM Employee e
        JOIN EmployeeRole er ON e.employee_id = er.employee_id
        JOIN Role r ON er.role_id = r.role_id
        WHERE 
            (:status IS NULL OR e.status = :status)
            AND (:roleName IS NULL OR r.role_name = :roleName)
            AND (
                :keyword IS NULL 
                OR e.full_name LIKE %:keyword%
                OR e.email LIKE %:keyword%
                OR e.phone_number LIKE %:keyword%
                OR e.username LIKE %:keyword%
            )
            AND r.role_name IN ('Order Staff', 'Material Staff')
            AND er.status = 'active'
            AND r.status = 'active'
        GROUP BY 
            e.employee_id, e.username, e.full_name, e.phone_number, 
            e.email, e.address, e.status, e.start_date, 
            e.date_of_birth, e.profile_picture_url, r.role_name, e.password
        ORDER BY
            CASE 
                WHEN :sortField = 'full_name' AND :sortDir = 'asc' THEN e.full_name
            END ASC,
            CASE 
                WHEN :sortField = 'full_name' AND :sortDir = 'desc' THEN e.full_name
            END DESC,
            CASE 
                WHEN :sortField = 'start_date' AND :sortDir = 'asc' THEN e.start_date
            END ASC,
            CASE 
                WHEN :sortField = 'start_date' AND :sortDir = 'desc' THEN e.start_date
            END DESC
        OFFSET :#{#pageable.offset} ROWS FETCH NEXT :#{#pageable.pageSize} ROWS ONLY
    """,
            countQuery = """ 
        SELECT COUNT(DISTINCT e.employee_id)
        FROM Employee e
        JOIN EmployeeRole er ON e.employee_id = er.employee_id
        JOIN Role r ON er.role_id = r.role_id
        WHERE 
            (:status IS NULL OR e.status = :status)
            AND (:roleName IS NULL OR r.role_name = :roleName)
            AND (
                :keyword IS NULL 
                OR e.full_name LIKE %:keyword%
                OR e.email LIKE %:keyword%
                OR e.phone_number LIKE %:keyword%
                OR e.username LIKE %:keyword%
            )
            AND er.status = 'active'
            AND r.status = 'active'
    """,
            nativeQuery = true
    )
    Page<Object[]> findEmployeesWithFilters(
            @Param("status") String status,
            @Param("roleName") String roleName,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortDir") String sortDir,
            Pageable pageable
    );

}
