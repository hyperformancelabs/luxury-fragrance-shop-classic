package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(name = "role_description", columnDefinition = "TEXT")
    private String roleDescription;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    // Getters and Setters
}
