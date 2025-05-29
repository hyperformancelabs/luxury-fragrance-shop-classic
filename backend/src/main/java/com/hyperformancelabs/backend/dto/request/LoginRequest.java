package com.hyperformancelabs.backend.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;

    // Constructor mặc định
    public LoginRequest() {
    }
}
