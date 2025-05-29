package com.hyperformancelabs.backend.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String phoneNumber;
    private String email;
}
