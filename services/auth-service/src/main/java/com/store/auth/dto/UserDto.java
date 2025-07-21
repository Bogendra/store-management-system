package com.store.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object for User entity, prevents recursion in JSON serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Long tenantId;
    private String tenantName;
    private Set<String> roleNames;
    private boolean enabled;
}
