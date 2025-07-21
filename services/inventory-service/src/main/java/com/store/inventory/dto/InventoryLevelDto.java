package com.store.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for InventoryLevel entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLevelDto {

    private Long id;
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    private String locationName;
    
    @NotNull(message = "Item variant ID is required")
    private Long itemVariantId;
    
    private String itemVariantSku;
    
    private String itemVariantName;
    
    private String itemName;
    
    @NotNull(message = "Quantity on hand is required")
    private BigDecimal quantityOnHand;
    
    private BigDecimal quantityReserved;
    
    private BigDecimal quantityAvailable;
    
    private BigDecimal reorderPoint;
    
    private BigDecimal reorderQuantity;
    
    private LocalDateTime lastCountedAt;
    
    private boolean lowStock;
}
