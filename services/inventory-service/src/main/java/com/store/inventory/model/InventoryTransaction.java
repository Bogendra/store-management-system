package com.store.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single inventory transaction, such as a receipt, sale, adjustment, or transfer.
 */
@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends BaseEntity {

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @NotNull(message = "Item variant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id", nullable = false)
    private ItemVariant itemVariant;
    
    @NotNull(message = "Transaction type is required")
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "reference_type")
    private String referenceType;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_by_user_id")
    private Long createdByUserId;
    
    /**
     * Types of inventory transactions.
     */
    public enum TransactionType {
        PURCHASE,       // Inventory received from purchase
        SALE,           // Inventory sold
        ADJUSTMENT,     // Inventory adjusted (e.g., due to damage, loss, etc.)
        TRANSFER_IN,    // Inventory transferred in from another location
        TRANSFER_OUT,   // Inventory transferred out to another location
        RETURN,         // Inventory returned by customer
        COUNT           // Inventory count adjustment
    }
}
