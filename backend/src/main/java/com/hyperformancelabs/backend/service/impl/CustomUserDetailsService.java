package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.CustomerDTO;
import com.hyperformancelabs.backend.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerService customerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomerDTO customer = customerService.getCustomerByEmailOrPhone(username, username);
        if (customer == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
        }
        return User.builder()
                .username(customer.getUsername())
                .password(customer.getPassword()) // đã mã hóa
                .roles("USER")
                .build();
    }
}
