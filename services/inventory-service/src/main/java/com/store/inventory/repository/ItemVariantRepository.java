package com.store.inventory.repository;

import com.store.inventory.model.EntityStatus;
import com.store.inventory.model.ItemVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ItemVariant entity operations.
 */
@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    /**
     * Find a variant by its SKU.
     */
    Optional<ItemVariant> findBySku(String sku);
    
    /**
     * Find all variants for a specific item.
     */
    List<ItemVariant> findAllByItemId(Long itemId);
    
    /**
     * Find all active variants for a specific item.
     */
    List<ItemVariant> findAllByItemIdAndStatus(Long itemId, EntityStatus status);
    
    /**
     * Find a specific variant by item ID and variant name.
     */
    Optional<ItemVariant> findByItemIdAndVariantName(Long itemId, String variantName);
    
    /**
     * Find all variants with pagination.
     */
    Page<ItemVariant> findAll(Pageable pageable);
    
    /**
     * Search variants by SKU (partial match).
     */
    @Query("SELECT iv FROM ItemVariant iv WHERE LOWER(iv.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ItemVariant> searchBySku(@Param("searchTerm") String searchTerm);
    
    /**
     * Check if a variant with the given SKU exists.
     */
    boolean existsBySku(String sku);
    
    /**
     * Find all variants for items belonging to a specific tenant.
     */
    @Query("SELECT iv FROM ItemVariant iv JOIN iv.item i WHERE i.tenantId = :tenantId")
    List<ItemVariant> findAllByTenantId(@Param("tenantId") Long tenantId);
}
