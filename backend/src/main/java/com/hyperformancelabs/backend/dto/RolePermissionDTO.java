package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO {

    private Integer rolePermissionId;

    private Integer roleId;

    private Integer permissionId;
}
