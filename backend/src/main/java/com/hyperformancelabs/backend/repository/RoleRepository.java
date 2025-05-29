package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.dto.RoleDTO;
import com.hyperformancelabs.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Tìm role theo tên role
    Role findByRoleName(String roleName);
}
