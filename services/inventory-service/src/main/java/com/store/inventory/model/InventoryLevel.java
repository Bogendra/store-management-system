package com.store.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks inventory levels for a specific item variant at a specific location.
 */
@Entity
@Table(name = "inventory_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLevel extends BaseEntity {

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @NotNull(message = "Item variant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id", nullable = false)
    private ItemVariant itemVariant;
    
    @NotNull(message = "Quantity on hand is required")
    @Column(name = "quantity_on_hand", nullable = false)
    private BigDecimal quantityOnHand;
    
    @NotNull(message = "Quantity reserved is required")
    @Column(name = "quantity_reserved", nullable = false)
    private BigDecimal quantityReserved;
    
    @Column(name = "reorder_point")
    private BigDecimal reorderPoint;
    
    @Column(name = "reorder_quantity")
    private BigDecimal reorderQuantity;
    
    @Column(name = "last_counted_at")
    private LocalDateTime lastCountedAt;
    
    /**
     * Get the quantity available for sale or use.
     * This is calculated as quantity on hand minus quantity reserved.
     */
    public BigDecimal getQuantityAvailable() {
        return quantityOnHand.subtract(quantityReserved);
    }
}
