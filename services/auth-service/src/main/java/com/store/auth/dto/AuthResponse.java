package com.store.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for authentication responses (JWT token).
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
