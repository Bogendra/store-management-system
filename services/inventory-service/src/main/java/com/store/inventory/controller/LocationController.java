package com.store.inventory.controller;

import com.store.inventory.dto.LocationDto;
import com.store.inventory.service.LocationService;
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
 * REST controller for location management operations.
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for managing inventory locations")
public class LocationController {

    private final LocationService locationService;
    
    /**
     * Get all locations for current tenant.
     */
    @GetMapping
    @Operation(summary = "Get all locations", description = "Retrieves all locations for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<LocationDto>> getAllLocations(
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<LocationDto> locations = locationService.getAllLocations(effectiveTenantId);
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Get locations by type.
     */
    @GetMapping("/by-type/{type}")
    @Operation(summary = "Get locations by type", description = "Retrieves all locations of a specific type")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<LocationDto>> getLocationsByType(
            @PathVariable String type,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        List<LocationDto> locations = locationService.getLocationsByType(type, effectiveTenantId);
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Get paginated locations for current tenant.
     */
    @GetMapping("/paged")
    @Operation(summary = "Get paginated locations", description = "Retrieves paginated locations for current tenant")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<LocationDto>> getPaginatedLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<LocationDto> locationsPage = locationService.getPaginatedLocations(effectiveTenantId, pageRequest);
        return ResponseEntity.ok(locationsPage);
    }
    
    /**
     * Get a specific location by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID", description = "Retrieves a specific location by its ID")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<LocationDto> getLocationById(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        LocationDto location = locationService.getLocationById(id, effectiveTenantId);
        return ResponseEntity.ok(location);
    }
    
    /**
     * Create a new location.
     */
    @PostMapping
    @Operation(summary = "Create location", description = "Creates a new location")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<LocationDto> createLocation(
            @Valid @RequestBody LocationDto locationDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        LocationDto createdLocation = locationService.createLocation(locationDto, effectiveTenantId);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing location.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update location", description = "Updates an existing location")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationDto locationDto,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        LocationDto updatedLocation = locationService.updateLocation(id, locationDto, effectiveTenantId);
        return ResponseEntity.ok(updatedLocation);
    }
    
    /**
     * Delete a location.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location", description = "Deletes a location (soft delete)")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real implementation, tenantId would be extracted from JWT claims
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        
        locationService.deleteLocation(id, effectiveTenantId);
        return ResponseEntity.noContent().build();
    }
}
