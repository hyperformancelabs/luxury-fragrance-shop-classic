package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "RolePermission", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_RolePermission", columnNames = {"role_id", "permission_id"})
})
@Data
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Integer rolePermissionId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id", referencedColumnName = "permission_id", nullable = false)
    private Permission permission;

    // Getters and Setters
}
