package com.hyperformancelabs.backend.dto.admin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Integer roleId;

    private String roleName;

    private String roleDescription;

    private Boolean isDefault;

    private String status;
} 