package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.RoleDTO;

public interface RoleService {

    // Tìm role theo tên role
    RoleDTO findRoleByRoleName(String roleName);
}
