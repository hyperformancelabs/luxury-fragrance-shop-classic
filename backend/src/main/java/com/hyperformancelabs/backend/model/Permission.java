package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Permission")
@Data
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    @Column(name = "permission_name", nullable = false, unique = true, length = 50)
    private String permissionName;

    @Column(name = "permission_description", columnDefinition = "TEXT")
    private String permissionDescription;

    // Getters and Setters
}
