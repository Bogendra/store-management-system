package com.store.inventory.repository;

import com.store.inventory.model.Brand;
import com.store.inventory.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Brand entity operations.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Find a brand by its name and tenant ID.
     */
    Optional<Brand> findByNameAndTenantId(String name, Long tenantId);
    
    /**
     * Find all active brands for a tenant.
     */
    List<Brand> findAllByTenantIdAndStatus(Long tenantId, EntityStatus status);
    
    /**
     * Find all brands for a tenant with pagination.
     */
    Page<Brand> findAllByTenantId(Long tenantId, Pageable pageable);
    
    /**
     * Check if a brand with the given name exists for a tenant.
     */
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
