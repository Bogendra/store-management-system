package com.store.inventory.repository;

import com.store.inventory.model.InventoryLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InventoryLevel entity operations.
 */
@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Long> {

    /**
     * Find inventory level for a specific item variant at a specific location.
     */
    Optional<InventoryLevel> findByLocationIdAndItemVariantId(Long locationId, Long itemVariantId);
    
    /**
     * Find all inventory levels for a specific location.
     */
    List<InventoryLevel> findAllByLocationId(Long locationId);
    
    /**
     * Find all inventory levels for a specific item variant across all locations.
     */
    List<InventoryLevel> findAllByItemVariantId(Long itemVariantId);
    
    /**
     * Find all inventory levels for a specific item across all locations.
     * Uses a join between ItemVariant and Item tables.
     */
    @Query("SELECT il FROM InventoryLevel il JOIN il.itemVariant iv WHERE iv.item.id = :itemId")
    List<InventoryLevel> findAllByItemId(@Param("itemId") Long itemId);
    
    /**
     * Find all inventory levels for a tenant (through Location's tenantId).
     */
    @Query("SELECT il FROM InventoryLevel il JOIN il.location l WHERE l.tenantId = :tenantId")
    List<InventoryLevel> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * Find all inventory levels with pagination for a tenant.
     */
    @Query("SELECT il FROM InventoryLevel il JOIN il.location l WHERE l.tenantId = :tenantId")
    Page<InventoryLevel> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * Find all low stock items (where quantity on hand is less than or equal to reorder point).
     */
    @Query("SELECT il FROM InventoryLevel il WHERE il.quantityOnHand <= il.reorderPoint AND il.reorderPoint > 0")
    List<InventoryLevel> findAllLowStockItems();
    
    /**
     * Find all low stock items for a specific tenant.
     */
    @Query("SELECT il FROM InventoryLevel il JOIN il.location l " +
            "WHERE l.tenantId = :tenantId AND il.quantityOnHand <= il.reorderPoint AND il.reorderPoint > 0")
    List<InventoryLevel> findAllLowStockItemsByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * Find all out-of-stock items (where quantity available is less than or equal to zero).
     */
    @Query("SELECT il FROM InventoryLevel il WHERE (il.quantityOnHand - il.quantityReserved) <= 0")
    List<InventoryLevel> findAllOutOfStockItems();
    
    /**
     * Find total quantity of an item variant across all locations.
     */
    @Query("SELECT SUM(il.quantityOnHand) FROM InventoryLevel il WHERE il.itemVariant.id = :itemVariantId")
    BigDecimal getTotalQuantityByItemVariantId(@Param("itemVariantId") Long itemVariantId);
    
    /**
     * Check if an inventory record exists for a specific item variant at a specific location.
     */
    boolean existsByLocationIdAndItemVariantId(Long locationId, Long itemVariantId);
}
