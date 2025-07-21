package com.store.inventory.model;

/**
 * Common status values for entities in the inventory system.
 */
public enum EntityStatus {
    ACTIVE,     // Entity is active and can be used
    INACTIVE,   // Entity is temporarily not in use but may be reactivated
    DELETED,    // Entity is marked for deletion (soft delete)
    DEPRECATED  // Entity is deprecated and should not be used for new data
}
