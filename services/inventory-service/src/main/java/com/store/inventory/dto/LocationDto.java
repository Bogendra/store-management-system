package com.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Location entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Long id;
    
    @NotBlank(message = "Location name is required")
    private String name;
    
    @NotNull(message = "Location type is required")
    private String type;
    
    private String address;
    
    private String status;
}
