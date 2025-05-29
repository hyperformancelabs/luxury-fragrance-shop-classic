package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.admin.common.RoleDTO;

public interface RoleService {

    // Tìm role theo tên role
    RoleDTO findRoleByRoleName(String roleName);
}
