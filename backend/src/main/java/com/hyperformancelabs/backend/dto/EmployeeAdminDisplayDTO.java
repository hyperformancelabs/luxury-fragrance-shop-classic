package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAdminDisplayDTO {
    private Integer employeeId;
    private String username;
    private String fullName;
    private String password;
    private String phoneNumber;
    private String email;
    private String address;
    private String status;
    private LocalDate startDate;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private String role;
}
