package com.store.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents a specific variant of an item, such as a particular size, color, or style.
 * Each variant has its own inventory levels and tracking.
 */
@Entity
@Table(name = "item_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @Column(name = "variant_name")
    private String variantName;
    
    @NotBlank(message = "SKU is required")
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;
}
