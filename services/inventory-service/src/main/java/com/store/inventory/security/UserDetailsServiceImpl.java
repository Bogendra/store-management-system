package com.store.inventory.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service provides user details for the security system.
 * In a real-world scenario, this would validate with the Auth Service,
 * but for now, we'll use a simple in-memory approach until we implement service-to-service auth.
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        // In a real implementation, this would validate JWT with Auth Service
        // For now, we trust the JWT validation and build user details from claims
        
        // Mock users for development until service-to-service auth is implemented
        if ("admin".equals(username)) {
            return createUserDetails(username, "INVENTORY_ADMIN", "INVENTORY_VIEW", "INVENTORY_EDIT");
        } else if ("manager".equals(username)) {
            return createUserDetails(username, "INVENTORY_VIEW", "INVENTORY_EDIT");
        } else if ("staff".equals(username)) {
            return createUserDetails(username, "INVENTORY_VIEW");
        }
        
        throw new UsernameNotFoundException("User not found: " + username);
    }
    
    private UserDetails createUserDetails(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        // Note: password is irrelevant as we're using JWT token validation
        return new User(username, "", grantedAuthorities);
    }
}
