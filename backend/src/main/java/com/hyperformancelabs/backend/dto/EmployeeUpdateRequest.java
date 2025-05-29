package com.hyperformancelabs.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeUpdateRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9 ()+-]+$", message = "Phone number contains invalid characters")
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String profilePictureUrl;
    
    // Trạng thái nhân viên (active, inactive, on_leave)
    private String status;
    
    // Optional field for changing password
    private String currentPassword;
    private String newPassword;
}
