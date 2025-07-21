package com.store.inventory.service;

import com.store.inventory.dto.ItemDto;
import com.store.inventory.dto.ItemVariantDto;
import com.store.inventory.exception.DuplicateResourceException;
import com.store.inventory.exception.ResourceNotFoundException;
import com.store.inventory.model.*;
import com.store.inventory.repository.BrandRepository;
import com.store.inventory.repository.CategoryRepository;
import com.store.inventory.repository.ItemRepository;
import com.store.inventory.repository.ItemVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing inventory items and their variants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new item with optional variants.
     */
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long tenantId) {
        log.debug("Creating item: {} for tenant: {}", itemDto.getName(), tenantId);
        
        // Check if item with same code already exists for this tenant
        if (itemRepository.existsByItemCodeAndTenantId(itemDto.getItemCode(), tenantId)) {
            throw new DuplicateResourceException("Item", "itemCode", itemDto.getItemCode());
        }
        
        // Check UPC code uniqueness if provided
        if (itemDto.getUpcCode() != null && 
            itemRepository.existsByUpcCodeAndTenantId(itemDto.getUpcCode(), tenantId)) {
            throw new DuplicateResourceException("Item", "upcCode", itemDto.getUpcCode());
        }
        
        Item item = new Item();
        item.setItemCode(itemDto.getItemCode());
        item.setUpcCode(itemDto.getUpcCode());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setStatus(EntityStatus.ACTIVE);
        item.setTenantId(tenantId);
        
        // Set brand if provided
        if (itemDto.getBrandId() != null) {
            Brand brand = brandRepository.findById(itemDto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", itemDto.getBrandId()));
            // Ensure brand belongs to tenant
            if (!brand.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Brand", "id", itemDto.getBrandId());
            }
            item.setBrand(brand);
        }
        
        // Set category if provided
        if (itemDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(itemDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", itemDto.getCategoryId()));
            // Ensure category belongs to tenant
            if (!category.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Category", "id", itemDto.getCategoryId());
            }
            item.setCategory(category);
        }
        
        Item savedItem = itemRepository.save(item);
        
        // Create variants if provided
        if (itemDto.getVariants() != null && !itemDto.getVariants().isEmpty()) {
            Set<ItemVariant> variants = new HashSet<>();
            for (ItemVariantDto variantDto : itemDto.getVariants()) {
                // Check SKU uniqueness
                if (itemVariantRepository.existsBySku(variantDto.getSku())) {
                    throw new DuplicateResourceException("ItemVariant", "sku", variantDto.getSku());
                }
                
                ItemVariant variant = new ItemVariant();
                variant.setItem(savedItem);
                variant.setVariantName(variantDto.getVariantName());
                variant.setSku(variantDto.getSku());
                variant.setStatus(EntityStatus.ACTIVE);
                
                variants.add(variant);
            }
            
            if (!variants.isEmpty()) {
                savedItem.setVariants(variants);
                itemVariantRepository.saveAll(variants);
            }
        }
        
        return mapToDto(savedItem);
    }
    
    /**
     * Get an item by ID.
     */
    public ItemDto getItemById(Long id, Long tenantId) {
        log.debug("Getting item with ID: {} for tenant: {}", id, tenantId);
        
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        
        // Ensure item belongs to tenant
        if (!item.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }
        
        return mapToDto(item);
    }
    
    /**
     * Get an item by its item code.
     */
    public ItemDto getItemByItemCode(String itemCode, Long tenantId) {
        log.debug("Getting item with code: {} for tenant: {}", itemCode, tenantId);
        
        Item item = itemRepository.findByItemCodeAndTenantId(itemCode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "itemCode", itemCode));
        
        return mapToDto(item);
    }
    
    /**
     * Update an existing item.
     */
    @Transactional
    public ItemDto updateItem(Long id, ItemDto itemDto, Long tenantId) {
        log.debug("Updating item with ID: {} for tenant: {}", id, tenantId);
        
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        
        // Ensure item belongs to tenant
        if (!item.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }
        
        // Check if item code change would conflict with existing item
        if (!item.getItemCode().equals(itemDto.getItemCode()) && 
            itemRepository.existsByItemCodeAndTenantId(itemDto.getItemCode(), tenantId)) {
            throw new DuplicateResourceException("Item", "itemCode", itemDto.getItemCode());
        }
        
        // Check if UPC code change would conflict with existing item
        if (itemDto.getUpcCode() != null && !Objects.equals(item.getUpcCode(), itemDto.getUpcCode()) && 
            itemRepository.existsByUpcCodeAndTenantId(itemDto.getUpcCode(), tenantId)) {
            throw new DuplicateResourceException("Item", "upcCode", itemDto.getUpcCode());
        }
        
        item.setItemCode(itemDto.getItemCode());
        item.setUpcCode(itemDto.getUpcCode());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        
        // Update brand if provided
        if (itemDto.getBrandId() != null) {
            Brand brand = brandRepository.findById(itemDto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", itemDto.getBrandId()));
            // Ensure brand belongs to tenant
            if (!brand.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Brand", "id", itemDto.getBrandId());
            }
            item.setBrand(brand);
        } else {
            item.setBrand(null);
        }
        
        // Update category if provided
        if (itemDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(itemDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", itemDto.getCategoryId()));
            // Ensure category belongs to tenant
            if (!category.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Category", "id", itemDto.getCategoryId());
            }
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }
        
        Item updatedItem = itemRepository.save(item);
        
        // Note: Variants are managed separately
        
        return mapToDto(updatedItem);
    }
    
    /**
     * Delete (deactivate) an item.
     */
    @Transactional
    public void deleteItem(Long id, Long tenantId) {
        log.debug("Deleting item with ID: {} for tenant: {}", id, tenantId);
        
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        
        // Ensure item belongs to tenant
        if (!item.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }
        
        // Soft delete by changing status
        item.setStatus(EntityStatus.DELETED);
        
        // Also mark all variants as deleted
        if (item.getVariants() != null) {
            for (ItemVariant variant : item.getVariants()) {
                variant.setStatus(EntityStatus.DELETED);
            }
        }
        
        itemRepository.save(item);
    }
    
    /**
     * Get all items for a tenant.
     */
    public List<ItemDto> getAllItems(Long tenantId) {
        log.debug("Getting all items for tenant: {}", tenantId);
        
        List<Item> items = itemRepository.findAllByTenantIdAndStatus(tenantId, EntityStatus.ACTIVE);
        return items.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get items by category.
     */
    public List<ItemDto> getItemsByCategory(Long categoryId, Long tenantId) {
        log.debug("Getting items for category: {} and tenant: {}", categoryId, tenantId);
        
        List<Item> items = itemRepository.findAllByCategoryIdAndTenantId(categoryId, tenantId);
        return items.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get items by brand.
     */
    public List<ItemDto> getItemsByBrand(Long brandId, Long tenantId) {
        log.debug("Getting items for brand: {} and tenant: {}", brandId, tenantId);
        
        List<Item> items = itemRepository.findAllByBrandIdAndTenantId(brandId, tenantId);
        return items.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Search items by name.
     */
    public Page<ItemDto> searchItemsByName(String searchTerm, Long tenantId, Pageable pageable) {
        log.debug("Searching items with term: {} for tenant: {}", searchTerm, tenantId);
        
        Page<Item> itemsPage = itemRepository.searchByName(tenantId, searchTerm, pageable);
        return itemsPage.map(this::mapToDto);
    }
    
    /**
     * Get paginated items for a tenant.
     */
    public Page<ItemDto> getPaginatedItems(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated items for tenant: {}", tenantId);
        
        Page<Item> itemsPage = itemRepository.findAllByTenantId(tenantId, pageable);
        return itemsPage.map(this::mapToDto);
    }
    
    /**
     * Add a variant to an existing item.
     */
    @Transactional
    public ItemVariantDto addVariantToItem(Long itemId, ItemVariantDto variantDto, Long tenantId) {
        log.debug("Adding variant to item: {} for tenant: {}", itemId, tenantId);
        
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        
        // Ensure item belongs to tenant
        if (!item.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
        
        // Check SKU uniqueness
        if (itemVariantRepository.existsBySku(variantDto.getSku())) {
            throw new DuplicateResourceException("ItemVariant", "sku", variantDto.getSku());
        }
        
        ItemVariant variant = new ItemVariant();
        variant.setItem(item);
        variant.setVariantName(variantDto.getVariantName());
        variant.setSku(variantDto.getSku());
        variant.setStatus(EntityStatus.ACTIVE);
        
        ItemVariant savedVariant = itemVariantRepository.save(variant);
        
        // Update the item's variants set
        if (item.getVariants() == null) {
            item.setVariants(new HashSet<>());
        }
        item.getVariants().add(savedVariant);
        
        return mapVariantToDto(savedVariant);
    }
    
    /**
     * Update an existing item variant.
     */
    @Transactional
    public ItemVariantDto updateVariant(Long variantId, ItemVariantDto variantDto, Long tenantId) {
        log.debug("Updating variant with ID: {}", variantId);
        
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", variantId));
        
        // Ensure variant's item belongs to tenant
        if (!variant.getItem().getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("ItemVariant", "id", variantId);
        }
        
        // Check SKU uniqueness if changed
        if (!variant.getSku().equals(variantDto.getSku()) && 
            itemVariantRepository.existsBySku(variantDto.getSku())) {
            throw new DuplicateResourceException("ItemVariant", "sku", variantDto.getSku());
        }
        
        variant.setVariantName(variantDto.getVariantName());
        variant.setSku(variantDto.getSku());
        
        ItemVariant updatedVariant = itemVariantRepository.save(variant);
        return mapVariantToDto(updatedVariant);
    }
    
    /**
     * Delete (deactivate) an item variant.
     */
    @Transactional
    public void deleteVariant(Long variantId, Long tenantId) {
        log.debug("Deleting variant with ID: {}", variantId);
        
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", variantId));
        
        // Ensure variant's item belongs to tenant
        if (!variant.getItem().getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("ItemVariant", "id", variantId);
        }
        
        // Soft delete by changing status
        variant.setStatus(EntityStatus.DELETED);
        itemVariantRepository.save(variant);
    }
    
    /**
     * Get all variants for an item.
     */
    public List<ItemVariantDto> getVariantsByItemId(Long itemId, Long tenantId) {
        log.debug("Getting variants for item: {} and tenant: {}", itemId, tenantId);
        
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        
        // Ensure item belongs to tenant
        if (!item.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
        
        List<ItemVariant> variants = itemVariantRepository.findAllByItemIdAndStatus(itemId, EntityStatus.ACTIVE);
        return variants.stream().map(this::mapVariantToDto).collect(Collectors.toList());
    }
    
    /**
     * Get a specific variant by ID.
     */
    public ItemVariantDto getVariantById(Long variantId, Long tenantId) {
        log.debug("Getting variant with ID: {}", variantId);
        
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", variantId));
        
        // Ensure variant's item belongs to tenant
        if (!variant.getItem().getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("ItemVariant", "id", variantId);
        }
        
        return mapVariantToDto(variant);
    }
    
    /**
     * Map Item entity to ItemDto.
     */
    private ItemDto mapToDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setItemCode(item.getItemCode());
        dto.setUpcCode(item.getUpcCode());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setStatus(item.getStatus().toString());
        
        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }
        
        if (item.getBrand() != null) {
            dto.setBrandId(item.getBrand().getId());
            dto.setBrandName(item.getBrand().getName());
        }
        
        if (item.getVariants() != null) {
            dto.setVariants(item.getVariants().stream()
                    .filter(v -> v.getStatus() == EntityStatus.ACTIVE)
                    .map(this::mapVariantToDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Map ItemVariant entity to ItemVariantDto.
     */
    private ItemVariantDto mapVariantToDto(ItemVariant variant) {
        ItemVariantDto dto = new ItemVariantDto();
        dto.setId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setVariantName(variant.getVariantName());
        dto.setStatus(variant.getStatus().toString());
        
        if (variant.getItem() != null) {
            dto.setItemId(variant.getItem().getId());
            dto.setItemName(variant.getItem().getName());
        }
        
        return dto;
    }
}
