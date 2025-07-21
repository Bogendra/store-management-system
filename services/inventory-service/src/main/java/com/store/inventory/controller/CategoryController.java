package com.store.inventory.controller;

import com.store.inventory.dto.CategoryDto;
import com.store.inventory.service.CategoryService;
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
 * REST controller for category management operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;
    
    /**
     * Get all categories for current tenant.
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<CategoryDto> categories = categoryService.getAllCategories(effectiveTenantId);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get top-level categories (categories without parents).
     */
    @GetMapping("/top-level")
    @Operation(summary = "Get top-level categories", description = "Retrieves all top-level categories (no parent)")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<CategoryDto>> getTopLevelCategories(
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<CategoryDto> categories = categoryService.getTopLevelCategories(effectiveTenantId);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get subcategories of a specific category.
     */
    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieves all subcategories of a specific category")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<CategoryDto>> getSubcategories(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<CategoryDto> subcategories = categoryService.getSubcategories(id, effectiveTenantId);
        return ResponseEntity.ok(subcategories);
    }
    
    /**
     * Get paginated categories for current tenant.
     */
    @GetMapping("/paged")
    @Operation(summary = "Get paginated categories", description = "Retrieves paginated categories for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<CategoryDto>> getPaginatedCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<CategoryDto> categoriesPage = categoryService.getPaginatedCategories(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(categoriesPage);
    }
    
    /**
     * Get a specific category by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<CategoryDto> getCategoryById(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        CategoryDto category = categoryService.getCategoryById(id, effectiveTenantId);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Create a new category.
     */
    @PostMapping
    @Operation(summary = "Create category", description = "Creates a new category")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        CategoryDto createdCategory = categoryService.createCategory(categoryDto, effectiveTenantId);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing category.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto, effectiveTenantId);
        return ResponseEntity.ok(updatedCategory);
    }
    
    /**
     * Delete a category.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category (soft delete)")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        categoryService.deleteCategory(id, effectiveTenantId);
        return ResponseEntity.noContent().build();
    }
}
