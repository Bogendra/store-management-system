package com.store.auth.service;

import com.store.auth.entity.User;
import com.store.auth.entity.Role;
import com.store.auth.entity.Tenant;
import com.store.auth.repository.UserRepository;
import com.store.auth.repository.RoleRepository;
import com.store.auth.repository.TenantRepository;
import com.store.auth.dto.UserRegistrationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

/**
 * Service for user management and registration.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the given details.
     */
    /**
     * Registers a new user for the current user's tenant (enforces tenant isolation).
     * If the current user is a global/brand admin, allows specifying tenantId.
     *
     * @param request registration request
     * @param currentUser the authenticated user performing the registration (can be null for public registration)
     * @param isGlobalAdmin true if the current user is a global/brand admin
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request, User currentUser, boolean isGlobalAdmin) {
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.getRole()));
        Long tenantId;
        if (isGlobalAdmin && request.getTenantId() != null) {
            tenantId = request.getTenantId();
        } else if (currentUser != null) {
            tenantId = currentUser.getTenant().getId();
        } else {
            tenantId = request.getTenantId(); // fallback for public registration
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .tenant(tenant)
                .roles(Collections.singleton(role))
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    /**
     * Legacy method for public registration only (no current user context).
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        return registerUser(request, null, false);
    }
}
