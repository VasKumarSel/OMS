package com.oms.orderservice.dto;

import com.oms.orderservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private UserRole role;
    private long expiresIn; // in seconds

    public LoginResponse(String token, Long userId, String username, UserRole role, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}
