package com.store.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.auth.dto.AuthRequest;
import com.store.auth.dto.UserRegistrationRequest;
import com.store.auth.entity.Role;
import com.store.auth.entity.Tenant;
import com.store.auth.entity.User;
import com.store.auth.repository.RoleRepository;
import com.store.auth.repository.TenantRepository;
import com.store.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AuthController endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant;
    private Role role;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        tenantRepository.deleteAll();
        tenant = tenantRepository.save(Tenant.builder().name("TestTenant").type("brand").build());
        role = roleRepository.save(Role.builder().name("USER").build());
    }

    @Test
    void registerAndLoginUser() throws Exception {
        // Register user
        UserRegistrationRequest regReq = new UserRegistrationRequest();
        regReq.setUsername("testuser");
        regReq.setPassword("password123");
        regReq.setEmail("test@example.com");
        regReq.setTenantId(tenant.getId());
        regReq.setRole(role.getName());
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());
        User user = userRepository.findByUsername("testuser").orElse(null);
        assertThat(user).isNotNull();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();

        // Login user
        AuthRequest authReq = new AuthRequest();
        authReq.setUsername("testuser");
        authReq.setPassword("password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk());
    }
}
