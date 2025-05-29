package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.RoleDTO;
import com.hyperformancelabs.backend.model.Role;
import com.hyperformancelabs.backend.repository.RoleRepository;
import com.hyperformancelabs.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleDTO findRoleByRoleName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role != null) {
            return convertToRoleDTO(role);
        }
        return null;
    }

    private RoleDTO convertToRoleDTO(Role role) {
        return new RoleDTO(
                role.getRoleId(),
                role.getRoleName(),
                role.getRoleDescription(),
                role.getIsDefault(),
                role.getStatus()
        );
    }
}
