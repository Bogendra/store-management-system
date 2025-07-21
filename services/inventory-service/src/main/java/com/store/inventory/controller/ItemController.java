package com.store.inventory.controller;

import com.store.inventory.dto.ItemDto;
import com.store.inventory.dto.ItemVariantDto;
import com.store.inventory.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for inventory item management operations.
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "APIs for managing inventory items and their variants")
public class ItemController {

    private final ItemService itemService;
    
    /**
     * Get all items for current tenant.
     */
    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieves all items for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<ItemDto>> getAllItems(
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<ItemDto> items = itemService.getAllItems(effectiveTenantId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get paginated items for current tenant.
     */
    @GetMapping("/paged")
    @Operation(summary = "Get paginated items", description = "Retrieves paginated items for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<ItemDto>> getPaginatedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ItemDto> itemsPage = itemService.getPaginatedItems(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(itemsPage);
    }
    
    /**
     * Search items by name.
     */
    @GetMapping("/search")
    @Operation(summary = "Search items by name", description = "Searches items by name for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<ItemDto>> searchItems(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemDto> itemsPage = itemService.searchItemsByName(searchTerm, effectiveTenantId, pageRequest);
        return ResponseEntity.ok(itemsPage);
    }
    
    /**
     * Get items by category.
     */
    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Get items by category", description = "Retrieves all items for a specific category")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<ItemDto>> getItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<ItemDto> items = itemService.getItemsByCategory(categoryId, effectiveTenantId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get items by brand.
     */
    @GetMapping("/by-brand/{brandId}")
    @Operation(summary = "Get items by brand", description = "Retrieves all items for a specific brand")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<ItemDto>> getItemsByBrand(
            @PathVariable Long brandId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<ItemDto> items = itemService.getItemsByBrand(brandId, effectiveTenantId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get a specific item by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves a specific item by its ID")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ItemDto> getItemById(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemDto item = itemService.getItemById(id, effectiveTenantId);
        return ResponseEntity.ok(item);
    }
    
    /**
     * Get an item by item code.
     */
    @GetMapping("/by-code/{itemCode}")
    @Operation(summary = "Get item by code", description = "Retrieves a specific item by its item code")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ItemDto> getItemByCode(
            @PathVariable String itemCode,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemDto item = itemService.getItemByItemCode(itemCode, effectiveTenantId);
        return ResponseEntity.ok(item);
    }
    
    /**
     * Create a new item.
     */
    @PostMapping
    @Operation(summary = "Create item", description = "Creates a new inventory item")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<ItemDto> createItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemDto createdItem = itemService.createItem(itemDto, effectiveTenantId);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing item.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Updates an existing inventory item")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<ItemDto> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDto itemDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemDto updatedItem = itemService.updateItem(id, itemDto, effectiveTenantId);
        return ResponseEntity.ok(updatedItem);
    }
    
    /**
     * Delete an item.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Deletes an item (soft delete)")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        itemService.deleteItem(id, effectiveTenantId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all variants for a specific item.
     */
    @GetMapping("/{itemId}/variants")
    @Operation(summary = "Get variants by item ID", description = "Retrieves all variants for a specific item")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<ItemVariantDto>> getVariantsByItemId(
            @PathVariable Long itemId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<ItemVariantDto> variants = itemService.getVariantsByItemId(itemId, effectiveTenantId);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get a specific variant by ID.
     */
    @GetMapping("/variants/{variantId}")
    @Operation(summary = "Get variant by ID", description = "Retrieves a specific variant by its ID")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ItemVariantDto> getVariantById(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemVariantDto variant = itemService.getVariantById(variantId, effectiveTenantId);
        return ResponseEntity.ok(variant);
    }
    
    /**
     * Add a new variant to an item.
     */
    @PostMapping("/{itemId}/variants")
    @Operation(summary = "Add variant to item", description = "Adds a new variant to an existing item")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<ItemVariantDto> addVariantToItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemVariantDto variantDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemVariantDto createdVariant = itemService.addVariantToItem(itemId, variantDto, effectiveTenantId);
        return new ResponseEntity<>(createdVariant, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing variant.
     */
    @PutMapping("/variants/{variantId}")
    @Operation(summary = "Update variant", description = "Updates an existing item variant")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<ItemVariantDto> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody ItemVariantDto variantDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        ItemVariantDto updatedVariant = itemService.updateVariant(variantId, variantDto, effectiveTenantId);
        return ResponseEntity.ok(updatedVariant);
    }
    
    /**
     * Delete a variant.
     */
    @DeleteMapping("/variants/{variantId}")
    @Operation(summary = "Delete variant", description = "Deletes an item variant (soft delete)")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        itemService.deleteVariant(variantId, effectiveTenantId);
        return ResponseEntity.noContent().build();
    }
}
