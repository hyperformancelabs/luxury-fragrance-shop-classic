package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.admin.common.EmployeeDTO;
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
        System.out.println("=== CustomAdminDetailsService.loadUserByUsername ===");
        System.out.println("Loading user by username: " + username);
        
        EmployeeDTO employee = employeeService.getEmployeeByUsername(username);

        if (employee == null) {
            System.out.println("Employee not found with username: " + username);
            throw new UsernameNotFoundException("Không tìm thấy admin: " + username);
        }

        System.out.println("Found employee: " + employee.getUsername() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Creating UserDetails with username: " + employee.getUsername());
        
        UserDetails userDetails = User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .roles("ADMIN")
                .build();
                
        System.out.println("UserDetails created successfully: " + userDetails.getUsername());
        System.out.println("=== END CustomAdminDetailsService.loadUserByUsername ===");
        
        return userDetails;
    }
}
