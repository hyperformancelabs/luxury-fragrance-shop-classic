package com.hyperformancelabs.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Integer employeeId;

    private String username;

    private String password;

    private String fullName;

    private String phoneNumber;

    private String email;

    private String address;

    private String status;

    private Date lastLogin;
    @DateTimeFormat(pattern = "yyyy-MM-dd") // ✅ Bắt buộc thêm
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // ✅ Bắt buộc thêm
    private Date dateOfBirth;

    private String profilePictureUrl;
}
