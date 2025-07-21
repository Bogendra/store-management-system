package com.store.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents a physical location where inventory is stored.
 * This could be a store, warehouse, or other storage location.
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location extends BaseEntity {

    @NotBlank(message = "Location name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotNull(message = "Location type is required")
    @Column(name = "type", nullable = false)
    private String type; // STORE, WAREHOUSE, etc.
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;
}
