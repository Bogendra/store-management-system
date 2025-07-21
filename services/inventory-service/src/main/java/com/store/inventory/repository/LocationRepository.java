package com.store.inventory.repository;

import com.store.inventory.model.EntityStatus;
import com.store.inventory.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entity operations.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find a location by name and tenant ID.
     */
    Optional<Location> findByNameAndTenantId(String name, Long tenantId);
    
    /**
     * Find all active locations for a tenant.
     */
    List<Location> findAllByTenantIdAndStatus(Long tenantId, EntityStatus status);
    
    /**
     * Find all locations of a specific type for a tenant.
     */
    List<Location> findAllByTenantIdAndType(Long tenantId, String type);
    
    /**
     * Find all locations for a tenant with pagination.
     */
    Page<Location> findAllByTenantId(Long tenantId, Pageable pageable);
    
    /**
     * Check if a location with the given name exists for a tenant.
     */
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
