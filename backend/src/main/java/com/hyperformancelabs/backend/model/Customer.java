package com.hyperformancelabs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "[Customer]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @NotBlank(message = "Name cannot be empty")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Pattern(regexp = "^[0-9 ()+-]+$", message = "Invalid phone number format")
    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100, unique = true, nullable = true)
    private String email;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "ward", length = 50)
    private String ward;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "shipping_note", length = 50)
    private String shippingNote;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @NotNull(message = "Rating cannot be empty")
    @Min(value = 0, message = "Rating cannot be negative")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "active|inactive|banned", message = "Status must be 'active', 'inactive' or 'banned'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull(message = "Loyalty points cannot be empty")
    @Min(value = 0, message = "Loyalty points cannot be negative")
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints;

    @NotNull(message = "Creation time cannot be empty")
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
