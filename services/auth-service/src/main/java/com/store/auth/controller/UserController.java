package com.store.auth.controller;

import com.store.auth.dto.UserDto;
import com.store.auth.entity.User;
import com.store.auth.entity.Role;
import com.store.auth.repository.UserRepository;
import com.store.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Endpoints for user info and RBAC examples.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get info about the currently authenticated user.
     * Accessible by any authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(convertToDto(user));
    }

    /**
     * Get all users. Only users with the 'USER_VIEW_ALL' privilege can access.
     */
    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        // Extract tenantId from the authenticated user's authorities/claims
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        Long tenantId = user.getTenant().getId();
        
        List<User> users = userRepository.findAllByTenantId(tenantId);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }
    
    /**
     * Converts User entity to UserDto to prevent recursion in JSON serialization
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setTenantId(user.getTenant().getId());
        dto.setTenantName(user.getTenant().getName());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);
        
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}
