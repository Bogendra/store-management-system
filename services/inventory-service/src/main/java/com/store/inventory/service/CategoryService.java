package com.store.inventory.service;

import com.store.inventory.dto.CategoryDto;
import com.store.inventory.exception.DuplicateResourceException;
import com.store.inventory.exception.ResourceNotFoundException;
import com.store.inventory.model.Category;
import com.store.inventory.model.EntityStatus;
import com.store.inventory.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    
    /**
     * Create a new category.
     */
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto, Long tenantId) {
        log.debug("Creating category: {} for tenant: {}", categoryDto.getName(), tenantId);
        
        // Check if category with same name already exists for this tenant
        if (categoryRepository.existsByNameAndTenantId(categoryDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Category", "name", categoryDto.getName());
        }
        
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setStatus(EntityStatus.ACTIVE);
        category.setTenantId(tenantId);
        
        // Set parent category if provided
        if (categoryDto.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", categoryDto.getParentId()));
            
            // Ensure parent category belongs to tenant
            if (!parentCategory.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Parent Category", "id", categoryDto.getParentId());
            }
            
            category.setParent(parentCategory);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }
    
    /**
     * Get a category by ID.
     */
    public CategoryDto getCategoryById(Long id, Long tenantId) {
        log.debug("Getting category with ID: {} for tenant: {}", id, tenantId);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Ensure category belongs to tenant
        if (!category.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        
        return mapToDtoWithChildren(category);
    }
    
    /**
     * Update an existing category.
     */
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto, Long tenantId) {
        log.debug("Updating category with ID: {} for tenant: {}", id, tenantId);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Ensure category belongs to tenant
        if (!category.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        
        // Check if name change would conflict with existing category
        if (!category.getName().equals(categoryDto.getName()) && 
            categoryRepository.existsByNameAndTenantId(categoryDto.getName(), tenantId)) {
            throw new DuplicateResourceException("Category", "name", categoryDto.getName());
        }
        
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        
        // Update parent category if provided
        if (categoryDto.getParentId() != null) {
            // Cannot set self as parent
            if (categoryDto.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            
            Category parentCategory = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", categoryDto.getParentId()));
            
            // Ensure parent category belongs to tenant
            if (!parentCategory.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Parent Category", "id", categoryDto.getParentId());
            }
            
            // Check if this would create a circular reference
            Category parent = parentCategory;
            while (parent != null) {
                if (parent.getId().equals(id)) {
                    throw new IllegalArgumentException("Setting this parent would create a circular reference");
                }
                parent = parent.getParent();
            }
            
            category.setParent(parentCategory);
        } else {
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        return mapToDtoWithChildren(updatedCategory);
    }
    
    /**
     * Delete (deactivate) a category.
     */
    @Transactional
    public void deleteCategory(Long id, Long tenantId) {
        log.debug("Deleting category with ID: {} for tenant: {}", id, tenantId);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Ensure category belongs to tenant
        if (!category.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        
        // Soft delete by changing status
        category.setStatus(EntityStatus.DELETED);
        categoryRepository.save(category);
    }
    
    /**
     * Get all top-level categories for a tenant.
     */
    public List<CategoryDto> getTopLevelCategories(Long tenantId) {
        log.debug("Getting top-level categories for tenant: {}", tenantId);
        
        List<Category> categories = categoryRepository.findAllByTenantIdAndParentIsNullAndStatus(
                tenantId, EntityStatus.ACTIVE);
        return categories.stream().map(this::mapToDtoWithChildren).collect(Collectors.toList());
    }
    
    /**
     * Get all categories for a tenant.
     */
    public List<CategoryDto> getAllCategories(Long tenantId) {
        log.debug("Getting all categories for tenant: {}", tenantId);
        
        List<Category> categories = categoryRepository.findAllByTenantIdAndStatus(tenantId, EntityStatus.ACTIVE);
        return categories.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get subcategories for a specific category.
     */
    public List<CategoryDto> getSubcategories(Long categoryId, Long tenantId) {
        log.debug("Getting subcategories for category: {} and tenant: {}", categoryId, tenantId);
        
        Category parentCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        // Ensure category belongs to tenant
        if (!parentCategory.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        List<Category> subcategories = categoryRepository.findAllByParentId(categoryId);
        return subcategories.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get paginated categories for a tenant.
     */
    public Page<CategoryDto> getPaginatedCategories(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated categories for tenant: {}", tenantId);
        
        Page<Category> categoriesPage = categoryRepository.findAllByTenantId(tenantId, pageable);
        return categoriesPage.map(this::mapToDto);
    }
    
    /**
     * Map Category entity to CategoryDto.
     */
    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.getStatus().toString());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        // Try to get full path if available
        try {
            String path = categoryRepository.getCategoryPath(category.getId());
            dto.setFullPath(path);
        } catch (Exception e) {
            log.warn("Could not retrieve full path for category {}: {}", category.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    /**
     * Map Category entity to CategoryDto with children.
     */
    private CategoryDto mapToDtoWithChildren(Category category) {
        CategoryDto dto = mapToDto(category);
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildren(new ArrayList<>());
        }
        
        return dto;
    }
}
