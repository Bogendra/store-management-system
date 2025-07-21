package com.store.inventory.service;

import com.store.inventory.dto.BrandDto;
import com.store.inventory.exception.DuplicateResourceException;
import com.store.inventory.exception.ResourceNotFoundException;
import com.store.inventory.model.Brand;
import com.store.inventory.model.EntityStatus;
import com.store.inventory.repository.BrandRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing brands.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    
    /**
     * Create a new brand.
     */
    @Transactional
    public BrandDto createBrand(BrandDto brandDto, Long tenantId) {
        log.debug("Creating brand: {} for tenant: {}", brandDto.getName(), tenantId);
        
        // Check if brand with same name already exists for this tenant
        if (brandRepository.existsByNameAndTenantId(brandDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Brand", "name", brandDto.getName());
        }
        
        Brand brand = new Brand();
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        brand.setStatus(EntityStatus.ACTIVE);
        brand.setTenantId(tenantId);
        
        Brand savedBrand = brandRepository.save(brand);
        return mapToDto(savedBrand);
    }
    
    /**
     * Get a brand by ID.
     */
    public BrandDto getBrandById(Long id, Long tenantId) {
        log.debug("Getting brand with ID: {} for tenant: {}", id, tenantId);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        
        // Ensure brand belongs to tenant
        if (!brand.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }
        
        return mapToDto(brand);
    }
    
    /**
     * Update an existing brand.
     */
    @Transactional
    public BrandDto updateBrand(Long id, BrandDto brandDto, Long tenantId) {
        log.debug("Updating brand with ID: {} for tenant: {}", id, tenantId);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        
        // Ensure brand belongs to tenant
        if (!brand.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }
        
        // Check if name change would conflict with existing brand
        if (!brand.getName().equals(brandDto.getName()) && 
            brandRepository.existsByNameAndTenantId(brandDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Brand", "name", brandDto.getName());
        }
        
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        
        Brand updatedBrand = brandRepository.save(brand);
        return mapToDto(updatedBrand);
    }
    
    /**
     * Delete (deactivate) a brand.
     */
    @Transactional
    public void deleteBrand(Long id, Long tenantId) {
        log.debug("Deleting brand with ID: {} for tenant: {}", id, tenantId);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        
        // Ensure brand belongs to tenant
        if (!brand.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }
        
        // Soft delete by changing status
        brand.setStatus(EntityStatus.DELETED);
        brandRepository.save(brand);
    }
    
    /**
     * Get all brands for a tenant.
     */
    public List<BrandDto> getAllBrands(Long tenantId) {
        log.debug("Getting all brands for tenant: {}", tenantId);
        
        List<Brand> brands = brandRepository.findAllByTenantIdAndStatus(tenantId, EntityStatus.ACTIVE);
        return brands.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get paginated brands for a tenant.
     */
    public Page<BrandDto> getPaginatedBrands(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated brands for tenant: {}", tenantId);
        
        Page<Brand> brandsPage = brandRepository.findAllByTenantId(tenantId, pageable);
        return brandsPage.map(this::mapToDto);
    }
    
    /**
     * Map Brand entity to BrandDto.
     */
    private BrandDto mapToDto(Brand brand) {
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .status(brand.getStatus().toString())
                .build();
    }
}
