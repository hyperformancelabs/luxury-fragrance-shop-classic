package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private String status;
    private LocalDate startDate;
    private LocalDateTime lastLogin;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private List<String> roles;

    // Custom constructor without specifying tokenType
    public LoginResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
    
    // Full constructor with all employee information
    public LoginResponse(String accessToken, Long id, String username, String fullName,
                         String phoneNumber, String email, String address, String status,
                         LocalDate startDate, LocalDateTime lastLogin, LocalDate dateOfBirth,
                         String profilePictureUrl, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.status = status;
        this.startDate = startDate;
        this.lastLogin = lastLogin;
        this.dateOfBirth = dateOfBirth;
        this.profilePictureUrl = profilePictureUrl;
        this.roles = roles;
    }
}
