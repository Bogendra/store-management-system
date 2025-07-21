package com.store.inventory.controller;

import com.store.inventory.dto.BrandDto;
import com.store.inventory.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * REST controller for brand management operations.
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "APIs for managing product brands")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

    private final BrandService brandService;
    
    /**
     * Get all brands for current tenant.
     */
    @GetMapping
    @Operation(summary = "Get all brands", description = "Retrieves all brands for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<BrandDto>> getAllBrands(@RequestParam(required = false) Long tenantId,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        // In a real implementation, tenantId would be extracted from JWT claims
        // For now, we'll use a placeholder value
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<BrandDto> brands = brandService.getAllBrands(effectiveTenantId);
        return ResponseEntity.ok(brands);
    }
    
    /**
     * Get paginated brands for current tenant.
     */
    @GetMapping("/paged")
    @Operation(summary = "Get paginated brands", description = "Retrieves paginated brands for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<BrandDto>> getPaginatedBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BrandDto> brandsPage = brandService.getPaginatedBrands(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(brandsPage);
    }
    
    /**
     * Get a specific brand by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", description = "Retrieves a specific brand by its ID")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<BrandDto> getBrandById(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        BrandDto brand = brandService.getBrandById(id, effectiveTenantId);
        return ResponseEntity.ok(brand);
    }
    
    /**
     * Create a new brand.
     */
    @PostMapping
    @Operation(summary = "Create brand", description = "Creates a new brand")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<BrandDto> createBrand(
            @Valid @RequestBody BrandDto brandDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        BrandDto createdBrand = brandService.createBrand(brandDto, effectiveTenantId);
        return new ResponseEntity<>(createdBrand, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing brand.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update brand", description = "Updates an existing brand")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<BrandDto> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandDto brandDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        BrandDto updatedBrand = brandService.updateBrand(id, brandDto, effectiveTenantId);
        return ResponseEntity.ok(updatedBrand);
    }
    
    /**
     * Delete a brand.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand", description = "Deletes a brand (soft delete)")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        brandService.deleteBrand(id, effectiveTenantId);
        return ResponseEntity.noContent().build();
    }
}
