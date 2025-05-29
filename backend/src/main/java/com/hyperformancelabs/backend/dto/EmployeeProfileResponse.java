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
public class EmployeeProfileResponse {
    private Long employeeId;
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
}
