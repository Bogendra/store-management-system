package com.store.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for InventoryTransaction entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDto {

    private Long id;
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    private String locationName;
    
    @NotNull(message = "Item variant ID is required")
    private Long itemVariantId;
    
    private String itemVariantSku;
    
    private String itemName;
    
    @NotNull(message = "Transaction type is required")
    private String transactionType;
    
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    
    private String referenceType;
    
    private String referenceId;
    
    private String notes;
    
    private Long createdByUserId;
    
    private String createdByUsername;
    
    private LocalDateTime createdAt;
}
