package com.store.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for authentication requests (login).
 */
@Data
public class AuthRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
