package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.model.EmployeeRole;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.repository.EmployeeRoleRepository;
import com.hyperformancelabs.backend.repository.RoleRepository;
import com.hyperformancelabs.backend.service.EmployeeRoleService;
import com.hyperformancelabs.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeRoleServiceImpl implements EmployeeRoleService {
    @Autowired
    private EmployeeRoleRepository employeeRoleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void addEmployeeRole(Integer employeeId, String roleName) {
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployee(employeeRepository.findByEmployeeId(employeeId));
        employeeRole.setRole(roleRepository.findByRoleName(roleName));
        employeeRoleRepository.save(employeeRole);
    }

    @Override
    public void deleteEmployeeRoleByEmployeeId(Integer employeeId) {
        employeeRoleRepository.deleteByEmployee_EmployeeId(employeeId);
    }
}
