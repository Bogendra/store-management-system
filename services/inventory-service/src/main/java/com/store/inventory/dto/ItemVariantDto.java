package com.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for ItemVariant entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantDto {

    private Long id;
    
    private Long itemId;
    
    private String itemName;
    
    private String variantName;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    private String status;
}
