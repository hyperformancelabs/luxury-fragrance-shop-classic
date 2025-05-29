package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.EmployeeDTO;
import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomAdminDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EmployeeDTO employee = employeeService.findActiveSystemAdminByEmailOrPhone(username);

        if (employee == null) {
            throw new UsernameNotFoundException("Không tìm thấy admin: " + username);
        }

        return User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .roles("ADMIN")
                .build();
    }
}
