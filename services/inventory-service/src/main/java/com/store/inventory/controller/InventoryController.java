package com.store.inventory.controller;

import com.store.inventory.dto.InventoryLevelDto;
import com.store.inventory.dto.InventoryTransactionDto;
import com.store.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for inventory management operations.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing inventory levels and transactions")
public class InventoryController {

    private final InventoryService inventoryService;
    
    /**
     * Get inventory level for a specific item variant at a specific location.
     */
    @GetMapping("/levels/location/{locationId}/variant/{variantId}")
    @Operation(summary = "Get inventory level", 
               description = "Retrieves inventory level for a specific item variant at a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<InventoryLevelDto> getInventoryLevel(
            @PathVariable Long locationId,
            @PathVariable Long variantId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        InventoryLevelDto inventoryLevel = inventoryService.getInventoryLevel(locationId, variantId, effectiveTenantId);
        return ResponseEntity.ok(inventoryLevel);
    }
    
    /**
     * Get all inventory levels for a specific location.
     */
    @GetMapping("/levels/location/{locationId}")
    @Operation(summary = "Get inventory levels by location", 
               description = "Retrieves all inventory levels for a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<InventoryLevelDto>> getInventoryLevelsByLocation(
            @PathVariable Long locationId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<InventoryLevelDto> inventoryLevels = inventoryService.getInventoryLevelsByLocation(locationId, effectiveTenantId);
        return ResponseEntity.ok(inventoryLevels);
    }
    
    /**
     * Get all inventory levels for a specific item variant across all locations.
     */
    @GetMapping("/levels/variant/{variantId}")
    @Operation(summary = "Get inventory levels by variant", 
               description = "Retrieves all inventory levels for a specific item variant across all locations")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<InventoryLevelDto>> getInventoryLevelsByItemVariant(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<InventoryLevelDto> inventoryLevels = inventoryService.getInventoryLevelsByItemVariant(variantId, effectiveTenantId);
        return ResponseEntity.ok(inventoryLevels);
    }
    
    /**
     * Get low stock items for a tenant.
     */
    @GetMapping("/levels/low-stock")
    @Operation(summary = "Get low stock items", 
               description = "Retrieves all items with stock levels below their reorder points")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<InventoryLevelDto>> getLowStockItems(
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<InventoryLevelDto> lowStockItems = inventoryService.getLowStockItems(effectiveTenantId);
        return ResponseEntity.ok(lowStockItems);
    }
    
    /**
     * Get paginated inventory levels.
     */
    @GetMapping("/levels/paged")
    @Operation(summary = "Get paginated inventory levels", 
               description = "Retrieves paginated inventory levels for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<InventoryLevelDto>> getPaginatedInventoryLevels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<InventoryLevelDto> inventoryLevelsPage = inventoryService.getPaginatedInventoryLevels(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(inventoryLevelsPage);
    }
    
    /**
     * Update inventory quantity.
     */
    @PostMapping("/update")
    @Operation(summary = "Update inventory quantity", 
               description = "Updates inventory quantity for a specific item variant at a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<InventoryLevelDto> updateInventoryQuantity(
            @RequestParam Long locationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal quantity,
            @RequestParam String transactionType,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId and userId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        Long userId = 1L; // Placeholder
        
        InventoryLevelDto updatedLevel = inventoryService.updateInventoryQuantity(
                locationId, itemVariantId, quantity, transactionType, 
                referenceType, referenceId, notes, userId, effectiveTenantId);
        return ResponseEntity.ok(updatedLevel);
    }
    
    /**
     * Reserve inventory.
     */
    @PostMapping("/reserve")
    @Operation(summary = "Reserve inventory", 
               description = "Reserves inventory for a specific item variant at a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<InventoryLevelDto> reserveInventory(
            @RequestParam Long locationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId and userId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        Long userId = 1L; // Placeholder
        
        InventoryLevelDto updatedLevel = inventoryService.reserveInventory(
                locationId, itemVariantId, quantity, referenceType, referenceId, userId, effectiveTenantId);
        return ResponseEntity.ok(updatedLevel);
    }
    
    /**
     * Release reserved inventory.
     */
    @PostMapping("/release")
    @Operation(summary = "Release reserved inventory", 
               description = "Releases previously reserved inventory for a specific item variant at a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<InventoryLevelDto> releaseReservedInventory(
            @RequestParam Long locationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId and userId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        Long userId = 1L; // Placeholder
        
        InventoryLevelDto updatedLevel = inventoryService.releaseReservedInventory(
                locationId, itemVariantId, quantity, referenceType, referenceId, userId, effectiveTenantId);
        return ResponseEntity.ok(updatedLevel);
    }
    
    /**
     * Transfer inventory between locations.
     */
    @PostMapping("/transfer")
    @Operation(summary = "Transfer inventory", 
               description = "Transfers inventory from one location to another")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> transferInventory(
            @RequestParam Long sourceLocationId,
            @RequestParam Long destinationLocationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId and userId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        Long userId = 1L; // Placeholder
        
        inventoryService.transferInventory(
                sourceLocationId, destinationLocationId, itemVariantId, quantity, 
                referenceId, notes, userId, effectiveTenantId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Set reorder point and quantity.
     */
    @PostMapping("/reorder-point")
    @Operation(summary = "Set reorder point", 
               description = "Sets reorder point and quantity for an inventory item")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<InventoryLevelDto> setReorderPoint(
            @RequestParam Long locationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal reorderPoint,
            @RequestParam BigDecimal reorderQuantity,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        InventoryLevelDto updatedLevel = inventoryService.setReorderPoint(
                locationId, itemVariantId, reorderPoint, reorderQuantity, effectiveTenantId);
        return ResponseEntity.ok(updatedLevel);
    }
    
    /**
     * Get inventory transactions for a specific item variant.
     */
    @GetMapping("/transactions/variant/{variantId}")
    @Operation(summary = "Get transactions by variant", 
               description = "Retrieves all inventory transactions for a specific item variant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<InventoryTransactionDto>> getTransactionsByItemVariant(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<InventoryTransactionDto> transactions = inventoryService.getTransactionsByItemVariant(variantId, effectiveTenantId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get inventory transactions for a specific location.
     */
    @GetMapping("/transactions/location/{locationId}")
    @Operation(summary = "Get transactions by location", 
               description = "Retrieves all inventory transactions for a specific location")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<InventoryTransactionDto>> getTransactionsByLocation(
            @PathVariable Long locationId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<InventoryTransactionDto> transactions = inventoryService.getTransactionsByLocation(locationId, effectiveTenantId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get paginated inventory transactions.
     */
    @GetMapping("/transactions/paged")
    @Operation(summary = "Get paginated transactions", 
               description = "Retrieves paginated inventory transactions for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<InventoryTransactionDto>> getPaginatedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
            
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<InventoryTransactionDto> transactionsPage = inventoryService.getPaginatedTransactions(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(transactionsPage);
    }
    
    /**
     * API endpoint for integration with POS systems to report a sale.
     */
    @PostMapping("/integration/pos/sale")
    @Operation(summary = "Process sale from POS", 
               description = "Processes a sale transaction from a POS system and updates inventory")
    public ResponseEntity<InventoryLevelDto> processSaleFromPos(
            @RequestParam Long locationId,
            @RequestParam Long itemVariantId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long tenantId) {
        
        // In a real implementation, tenantId would be validated through API keys or other means
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        Long systemUserId = 0L; // System user
        
        InventoryLevelDto updatedLevel = inventoryService.updateInventoryQuantity(
                locationId, itemVariantId, quantity.negate(), "SALE", 
                "ORDER", orderId, "Sale processed through POS integration", systemUserId, effectiveTenantId);
        return ResponseEntity.ok(updatedLevel);
    }
}
