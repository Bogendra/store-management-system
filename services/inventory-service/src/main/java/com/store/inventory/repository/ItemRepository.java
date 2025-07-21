package com.store.inventory.repository;

import com.store.inventory.model.EntityStatus;
import com.store.inventory.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Item entity operations.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find an item by its code for a specific tenant.
     */
    Optional<Item> findByItemCodeAndTenantId(String itemCode, Long tenantId);
    
    /**
     * Find an item by UPC code for a specific tenant.
     */
    Optional<Item> findByUpcCodeAndTenantId(String upcCode, Long tenantId);
    
    /**
     * Find all items for a specific brand and tenant.
     */
    List<Item> findAllByBrandIdAndTenantId(Long brandId, Long tenantId);
    
    /**
     * Find all items in a specific category and tenant.
     */
    List<Item> findAllByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    
    /**
     * Find all active items for a tenant.
     */
    List<Item> findAllByTenantIdAndStatus(Long tenantId, EntityStatus status);
    
    /**
     * Search items by name (partial match) for a tenant.
     */
    @Query("SELECT i FROM Item i WHERE i.tenantId = :tenantId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Item> searchByName(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find all items for a tenant with pagination.
     */
    Page<Item> findAllByTenantId(Long tenantId, Pageable pageable);
    
    /**
     * Check if an item with the given code exists for a tenant.
     */
    boolean existsByItemCodeAndTenantId(String itemCode, Long tenantId);
    
    /**
     * Check if an item with the given UPC code exists for a tenant.
     */
    boolean existsByUpcCodeAndTenantId(String upcCode, Long tenantId);
}
