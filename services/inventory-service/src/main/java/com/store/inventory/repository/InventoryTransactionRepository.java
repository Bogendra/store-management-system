package com.store.inventory.repository;

import com.store.inventory.model.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryTransaction entity operations.
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Find all transactions for a specific item variant.
     */
    List<InventoryTransaction> findAllByItemVariantId(Long itemVariantId);
    
    /**
     * Find all transactions for a specific location.
     */
    List<InventoryTransaction> findAllByLocationId(Long locationId);
    
    /**
     * Find all transactions for a specific item variant at a specific location.
     */
    List<InventoryTransaction> findAllByLocationIdAndItemVariantId(Long locationId, Long itemVariantId);
    
    /**
     * Find all transactions by transaction type.
     */
    List<InventoryTransaction> findAllByTransactionType(InventoryTransaction.TransactionType transactionType);
    
    /**
     * Find all transactions for a specific item variant within a date range.
     */
    List<InventoryTransaction> findAllByItemVariantIdAndCreatedAtBetween(
            Long itemVariantId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all transactions for a specific location within a date range.
     */
    List<InventoryTransaction> findAllByLocationIdAndCreatedAtBetween(
            Long locationId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all transactions with pagination.
     */
    Page<InventoryTransaction> findAll(Pageable pageable);
    
    /**
     * Find all transactions by reference type and reference ID.
     * For example, all transactions related to a specific purchase order.
     */
    List<InventoryTransaction> findAllByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    
    /**
     * Find all transactions for a specific tenant (through Location's tenantId).
     */
    @Query("SELECT t FROM InventoryTransaction t JOIN t.location l WHERE l.tenantId = :tenantId")
    List<InventoryTransaction> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * Find all transactions for a specific tenant with pagination.
     */
    @Query("SELECT t FROM InventoryTransaction t JOIN t.location l WHERE l.tenantId = :tenantId")
    Page<InventoryTransaction> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * Find all transactions created by a specific user.
     */
    List<InventoryTransaction> findAllByCreatedByUserId(Long userId);
    
    /**
     * Find all transactions for a specific item.
     * Uses a join between ItemVariant and Item tables.
     */
    @Query("SELECT t FROM InventoryTransaction t JOIN t.itemVariant iv WHERE iv.item.id = :itemId")
    List<InventoryTransaction> findAllByItemId(@Param("itemId") Long itemId);
}
