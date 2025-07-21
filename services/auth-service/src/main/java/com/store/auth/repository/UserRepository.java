package com.store.auth.repository;

import com.store.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity, with tenant-based filtering.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Find all users for a specific tenant
    List<User> findAllByTenantId(Long tenantId);

    // Find user by username and tenant
    Optional<User> findByUsernameAndTenantId(String username, Long tenantId);
}
