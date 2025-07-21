package com.store.inventory.service;

import com.store.inventory.dto.LocationDto;
import com.store.inventory.exception.DuplicateResourceException;
import com.store.inventory.exception.ResourceNotFoundException;
import com.store.inventory.model.EntityStatus;
import com.store.inventory.model.Location;
import com.store.inventory.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    
    /**
     * Create a new location.
     */
    @Transactional
    public LocationDto createLocation(LocationDto locationDto, Long tenantId) {
        log.debug("Creating location: {} for tenant: {}", locationDto.getName(), tenantId);
        
        // Check if location with same name already exists for this tenant
        if (locationRepository.existsByNameAndTenantId(locationDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Location", "name", locationDto.getName());
        }
        
        Location location = new Location();
        location.setName(locationDto.getName());
        location.setType(locationDto.getType());
        location.setAddress(locationDto.getAddress());
        location.setStatus(EntityStatus.ACTIVE);
        location.setTenantId(tenantId);
        
        Location savedLocation = locationRepository.save(location);
        return mapToDto(savedLocation);
    }
    
    /**
     * Get a location by ID.
     */
    public LocationDto getLocationById(Long id, Long tenantId) {
        log.debug("Getting location with ID: {} for tenant: {}", id, tenantId);
        
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        // Ensure location belongs to tenant
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", id);
        }
        
        return mapToDto(location);
    }
    
    /**
     * Update an existing location.
     */
    @Transactional
    public LocationDto updateLocation(Long id, LocationDto locationDto, Long tenantId) {
        log.debug("Updating location with ID: {} for tenant: {}", id, tenantId);
        
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        // Ensure location belongs to tenant
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", id);
        }
        
        // Check if name change would conflict with existing location
        if (!location.getName().equals(locationDto.getName()) && 
            locationRepository.existsByNameAndTenantId(locationDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Location", "name", locationDto.getName());
        }
        
        location.setName(locationDto.getName());
        location.setType(locationDto.getType());
        location.setAddress(locationDto.getAddress());
        
        Location updatedLocation = locationRepository.save(location);
        return mapToDto(updatedLocation);
    }
    
    /**
     * Delete (deactivate) a location.
     */
    @Transactional
    public void deleteLocation(Long id, Long tenantId) {
        log.debug("Deleting location with ID: {} for tenant: {}", id, tenantId);
        
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        // Ensure location belongs to tenant
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", id);
        }
        
        // Soft delete by changing status
        location.setStatus(EntityStatus.DELETED);
        locationRepository.save(location);
    }
    
    /**
     * Get all locations for a tenant.
     */
    public List<LocationDto> getAllLocations(Long tenantId) {
        log.debug("Getting all locations for tenant: {}", tenantId);
        
        List<Location> locations = locationRepository.findAllByTenantIdAndStatus(tenantId, EntityStatus.ACTIVE);
        return locations.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get all locations of a specific type for a tenant.
     */
    public List<LocationDto> getLocationsByType(String type, Long tenantId) {
        log.debug("Getting locations of type: {} for tenant: {}", type, tenantId);
        
        List<Location> locations = locationRepository.findAllByTenantIdAndType(tenantId, type);
        return locations.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get paginated locations for a tenant.
     */
    public Page<LocationDto> getPaginatedLocations(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated locations for tenant: {}", tenantId);
        
        Page<Location> locationsPage = locationRepository.findAllByTenantId(tenantId, pageable);
        return locationsPage.map(this::mapToDto);
    }
    
    /**
     * Map Location entity to LocationDto.
     */
    private LocationDto mapToDto(Location location) {
        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .type(location.getType())
                .address(location.getAddress())
                .status(location.getStatus().toString())
                .build();
    }
}
