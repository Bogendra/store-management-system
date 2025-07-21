package com.store.auth.controller;

import com.store.auth.dto.UserDto;
import com.store.auth.dto.UserRegistrationRequest;
import com.store.auth.entity.Role;
import com.store.auth.entity.Tenant;
import com.store.auth.entity.User;
import com.store.auth.repository.RoleRepository;
import com.store.auth.repository.TenantRepository;
import com.store.auth.repository.UserRepository;
import com.store.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin endpoints for user, role, and tenant management.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    
    /**
     * Get all roles - available to super admins only
     */
    @PreAuthorize("hasAuthority('ROLE_VIEW_ALL')")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
    
    /**
     * Get all tenants - available to super admins only
     */
    @PreAuthorize("hasAuthority('TENANT_VIEW_ALL')")
    @GetMapping("/tenants")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }
    
    /**
     * Create new user - available to super admins and managers with USER_CREATE privilege
     */
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(
            @RequestBody UserRegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get the current user
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Check if current user is a super admin (has SUPER_ADMIN role)
        boolean isGlobalAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getName()));
        
        // Register the user with tenant isolation logic
        User newUser = userService.registerUser(request, currentUser, isGlobalAdmin);
        
        // Convert to DTO and return
        UserDto userDto = convertToDto(newUser);
        return ResponseEntity.ok(userDto);
    }
    
    /**
     * Converts User entity to UserDto
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
