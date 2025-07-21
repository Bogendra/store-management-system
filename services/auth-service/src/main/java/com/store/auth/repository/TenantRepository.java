package com.store.auth.repository;

import com.store.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for Tenant entity.
 */
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByParentTenantId(Long parentTenantId);
}
