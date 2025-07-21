package com.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for Item entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    private String upcCode;
    
    @NotBlank(message = "Item name is required")
    private String name;
    
    private String description;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Long brandId;
    
    private String brandName;
    
    private List<ItemVariantDto> variants;
    
    private String status;
}
