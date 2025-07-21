package com.store.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

/**
 * Represents a tenant (brand or outlet) in the system. Supports parent-child hierarchy.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Parent tenant for hierarchy (brand/outlet relationship) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tenant_id")
    private Tenant parentTenant;

    @OneToMany(mappedBy = "parentTenant")
    private Set<Tenant> childTenants;

    @Column(nullable = false)
    private String type; // e.g., brand, outlet
}
