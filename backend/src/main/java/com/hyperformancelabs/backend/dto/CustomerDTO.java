package com.hyperformancelabs.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Integer customerId;

    @Size(max = 50, message = "Username cannot exceed 50 characters")
    private String username;

    private String password; // Có thể ẩn khi không cần truyền lên

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Pattern(regexp = "^[0-9 ()+-]+$", message = "Invalid phone number format")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private String street;
    private String ward;
    private String district;
    private String city;
    private String shippingNote;

    private String note;

    @NotNull(message = "Rating is required")
    @Min(value = 0, message = "Rating cannot be negative")
    private Integer rating;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "active|inactive|banned", message = "Status must be 'active', 'inactive' or 'banned'")
    private String status;

    @NotNull(message = "Loyalty points are required")
    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPoints;

    @NotNull(message = "Creation time is required")
    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
