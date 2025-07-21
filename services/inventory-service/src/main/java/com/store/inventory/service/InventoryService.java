package com.store.inventory.service;

import com.store.inventory.dto.InventoryLevelDto;
import com.store.inventory.dto.InventoryTransactionDto;
import com.store.inventory.exception.InsufficientInventoryException;
import com.store.inventory.exception.ResourceNotFoundException;
import com.store.inventory.model.*;
import com.store.inventory.repository.InventoryLevelRepository;
import com.store.inventory.repository.InventoryTransactionRepository;
import com.store.inventory.repository.ItemVariantRepository;
import com.store.inventory.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing inventory levels and transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryLevelRepository inventoryLevelRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final LocationRepository locationRepository;
    private final ItemVariantRepository itemVariantRepository;
    
    /**
     * Get inventory level for a specific item variant at a specific location.
     */
    public InventoryLevelDto getInventoryLevel(Long locationId, Long itemVariantId, Long tenantId) {
        log.debug("Getting inventory level for location: {} and item variant: {}", locationId, itemVariantId);
        
        // Verify location belongs to tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        // Verify item variant exists
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Get inventory level or return zero quantities if not found
        InventoryLevel inventoryLevel = inventoryLevelRepository
                .findByLocationIdAndItemVariantId(locationId, itemVariantId)
                .orElse(null);
        
        if (inventoryLevel == null) {
            return createEmptyInventoryLevelDto(location, itemVariant);
        }
        
        return mapToDto(inventoryLevel);
    }
    
    /**
     * Get all inventory levels for a specific location.
     */
    public List<InventoryLevelDto> getInventoryLevelsByLocation(Long locationId, Long tenantId) {
        log.debug("Getting inventory levels for location: {}", locationId);
        
        // Verify location belongs to tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        List<InventoryLevel> inventoryLevels = inventoryLevelRepository.findAllByLocationId(locationId);
        return inventoryLevels.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get all inventory levels for a specific item variant across all locations.
     */
    public List<InventoryLevelDto> getInventoryLevelsByItemVariant(Long itemVariantId, Long tenantId) {
        log.debug("Getting inventory levels for item variant: {}", itemVariantId);
        
        // Verify item variant exists
        itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        List<InventoryLevel> inventoryLevels = inventoryLevelRepository.findAllByItemVariantId(itemVariantId);
        
        // Filter inventory levels by tenant through locations
        return inventoryLevels.stream()
                .filter(level -> level.getLocation().getTenantId().equals(tenantId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get low stock items for a tenant.
     */
    public List<InventoryLevelDto> getLowStockItems(Long tenantId) {
        log.debug("Getting low stock items for tenant: {}", tenantId);
        
        List<InventoryLevel> lowStockItems = inventoryLevelRepository.findAllLowStockItemsByTenantId(tenantId);
        return lowStockItems.stream().map(level -> {
            InventoryLevelDto dto = mapToDto(level);
            dto.setLowStock(true);
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get paginated inventory levels for a tenant.
     */
    public Page<InventoryLevelDto> getPaginatedInventoryLevels(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated inventory levels for tenant: {}", tenantId);
        
        Page<InventoryLevel> inventoryLevelsPage = inventoryLevelRepository.findAllByTenantId(tenantId, pageable);
        return inventoryLevelsPage.map(this::mapToDto);
    }
    
    /**
     * Update inventory quantity for a specific item variant at a specific location.
     */
    @Transactional
    public InventoryLevelDto updateInventoryQuantity(Long locationId, Long itemVariantId, 
                                                 BigDecimal quantityChange, String transactionType,
                                                 String referenceType, String referenceId, 
                                                 String notes, Long userId, Long tenantId) {
        log.debug("Updating inventory quantity for location: {} and item variant: {} by: {}", 
                locationId, itemVariantId, quantityChange);
        
        // Validate location and tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        // Validate item variant
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Get or create inventory level
        InventoryLevel inventoryLevel = inventoryLevelRepository
                .findByLocationIdAndItemVariantId(locationId, itemVariantId)
                .orElse(createNewInventoryLevel(location, itemVariant));
        
        // Check if we have enough inventory for negative adjustments
        if (quantityChange.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal availableQty = inventoryLevel.getQuantityOnHand().subtract(inventoryLevel.getQuantityReserved());
            if (availableQty.abs().compareTo(quantityChange.abs()) < 0) {
                throw new InsufficientInventoryException(itemVariant.getSku(), location.getName(), 
                        availableQty, quantityChange.abs());
            }
        }
        
        // Update inventory level
        inventoryLevel.setQuantityOnHand(inventoryLevel.getQuantityOnHand().add(quantityChange));
        
        // For inventory counts, update the last counted timestamp
        if ("COUNT".equals(transactionType)) {
            inventoryLevel.setLastCountedAt(LocalDateTime.now());
        }
        
        // Save the inventory level
        InventoryLevel updatedLevel = inventoryLevelRepository.save(inventoryLevel);
        
        // Create a transaction record
        createInventoryTransaction(location, itemVariant, 
                InventoryTransaction.TransactionType.valueOf(transactionType), 
                quantityChange, referenceType, referenceId, notes, userId);
        
        return mapToDto(updatedLevel);
    }
    
    /**
     * Reserve inventory for a specific item variant at a specific location.
     */
    @Transactional
    public InventoryLevelDto reserveInventory(Long locationId, Long itemVariantId, 
                                          BigDecimal quantity, String referenceType, 
                                          String referenceId, Long userId, Long tenantId) {
        log.debug("Reserving inventory quantity {} for location: {} and item variant: {}", 
                quantity, locationId, itemVariantId);
        
        // Validate location and tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        // Validate item variant
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Get or create inventory level
        InventoryLevel inventoryLevel = inventoryLevelRepository
                .findByLocationIdAndItemVariantId(locationId, itemVariantId)
                .orElse(createNewInventoryLevel(location, itemVariant));
        
        // Check if we have enough inventory to reserve
        BigDecimal availableQty = inventoryLevel.getQuantityOnHand().subtract(inventoryLevel.getQuantityReserved());
        if (availableQty.compareTo(quantity) < 0) {
            throw new InsufficientInventoryException(itemVariant.getSku(), location.getName(), 
                    availableQty, quantity);
        }
        
        // Update inventory level
        inventoryLevel.setQuantityReserved(inventoryLevel.getQuantityReserved().add(quantity));
        
        // Save the inventory level
        InventoryLevel updatedLevel = inventoryLevelRepository.save(inventoryLevel);
        
        // Create a transaction record for the reservation
        createInventoryTransaction(location, itemVariant, 
                InventoryTransaction.TransactionType.TRANSFER_OUT, 
                BigDecimal.ZERO, referenceType, referenceId, 
                "Reserved " + quantity + " units", userId);
        
        return mapToDto(updatedLevel);
    }
    
    /**
     * Release reserved inventory for a specific item variant at a specific location.
     */
    @Transactional
    public InventoryLevelDto releaseReservedInventory(Long locationId, Long itemVariantId, 
                                                  BigDecimal quantity, String referenceType, 
                                                  String referenceId, Long userId, Long tenantId) {
        log.debug("Releasing reserved inventory quantity {} for location: {} and item variant: {}", 
                quantity, locationId, itemVariantId);
        
        // Validate location and tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        // Validate item variant
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Get inventory level
        InventoryLevel inventoryLevel = inventoryLevelRepository
                .findByLocationIdAndItemVariantId(locationId, itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLevel", "locationId and itemVariantId", 
                        locationId + "," + itemVariantId));
        
        // Check if we have enough reserved inventory to release
        if (inventoryLevel.getQuantityReserved().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Cannot release more than the reserved quantity. " +
                    "Reserved: " + inventoryLevel.getQuantityReserved() + ", Requested: " + quantity);
        }
        
        // Update inventory level
        inventoryLevel.setQuantityReserved(inventoryLevel.getQuantityReserved().subtract(quantity));
        
        // Save the inventory level
        InventoryLevel updatedLevel = inventoryLevelRepository.save(inventoryLevel);
        
        // Create a transaction record for the reservation release
        createInventoryTransaction(location, itemVariant, 
                InventoryTransaction.TransactionType.TRANSFER_IN, 
                BigDecimal.ZERO, referenceType, referenceId, 
                "Released " + quantity + " units from reservation", userId);
        
        return mapToDto(updatedLevel);
    }
    
    /**
     * Transfer inventory between locations.
     */
    @Transactional
    public void transferInventory(Long sourceLocationId, Long destinationLocationId, 
                               Long itemVariantId, BigDecimal quantity, 
                               String referenceId, String notes, Long userId, Long tenantId) {
        log.debug("Transferring inventory quantity {} from location: {} to location: {} for item variant: {}", 
                quantity, sourceLocationId, destinationLocationId, itemVariantId);
        
        // Validate source location and tenant
        Location sourceLocation = locationRepository.findById(sourceLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Source Location", "id", sourceLocationId));
        if (!sourceLocation.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Source Location", "id", sourceLocationId);
        }
        
        // Validate destination location and tenant
        Location destLocation = locationRepository.findById(destinationLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination Location", "id", destinationLocationId));
        if (!destLocation.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Destination Location", "id", destinationLocationId);
        }
        
        // Validate item variant
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Decrease inventory at source location
        updateInventoryQuantity(sourceLocationId, itemVariantId, quantity.negate(), 
                "TRANSFER_OUT", "TRANSFER", referenceId, 
                "Transfer to " + destLocation.getName() + ": " + notes, userId, tenantId);
        
        // Increase inventory at destination location
        updateInventoryQuantity(destinationLocationId, itemVariantId, quantity, 
                "TRANSFER_IN", "TRANSFER", referenceId, 
                "Transfer from " + sourceLocation.getName() + ": " + notes, userId, tenantId);
    }
    
    /**
     * Set reorder point and quantity for an inventory item.
     */
    @Transactional
    public InventoryLevelDto setReorderPoint(Long locationId, Long itemVariantId, 
                                         BigDecimal reorderPoint, BigDecimal reorderQuantity, 
                                         Long tenantId) {
        log.debug("Setting reorder point to {} and quantity to {} for location: {} and item variant: {}", 
                reorderPoint, reorderQuantity, locationId, itemVariantId);
        
        // Validate location and tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        // Validate item variant
        ItemVariant itemVariant = itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        // Get or create inventory level
        InventoryLevel inventoryLevel = inventoryLevelRepository
                .findByLocationIdAndItemVariantId(locationId, itemVariantId)
                .orElse(createNewInventoryLevel(location, itemVariant));
        
        // Update reorder settings
        inventoryLevel.setReorderPoint(reorderPoint);
        inventoryLevel.setReorderQuantity(reorderQuantity);
        
        // Save the inventory level
        InventoryLevel updatedLevel = inventoryLevelRepository.save(inventoryLevel);
        
        return mapToDto(updatedLevel);
    }
    
    /**
     * Get inventory transactions for a specific item variant.
     */
    public List<InventoryTransactionDto> getTransactionsByItemVariant(Long itemVariantId, Long tenantId) {
        log.debug("Getting transactions for item variant: {}", itemVariantId);
        
        // Verify item variant exists
        itemVariantRepository.findById(itemVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemVariant", "id", itemVariantId));
        
        List<InventoryTransaction> transactions = transactionRepository.findAllByItemVariantId(itemVariantId);
        
        // Filter transactions by tenant through locations
        return transactions.stream()
                .filter(tx -> tx.getLocation().getTenantId().equals(tenantId))
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get inventory transactions for a specific location.
     */
    public List<InventoryTransactionDto> getTransactionsByLocation(Long locationId, Long tenantId) {
        log.debug("Getting transactions for location: {}", locationId);
        
        // Verify location belongs to tenant
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
        if (!location.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Location", "id", locationId);
        }
        
        List<InventoryTransaction> transactions = transactionRepository.findAllByLocationId(locationId);
        return transactions.stream().map(this::mapTransactionToDto).collect(Collectors.toList());
    }
    
    /**
     * Get paginated inventory transactions for a tenant.
     */
    public Page<InventoryTransactionDto> getPaginatedTransactions(Long tenantId, Pageable pageable) {
        log.debug("Getting paginated transactions for tenant: {}", tenantId);
        
        Page<InventoryTransaction> transactionsPage = transactionRepository.findAllByTenantId(tenantId, pageable);
        return transactionsPage.map(this::mapTransactionToDto);
    }
    
    /**
     * Create a new inventory level object.
     */
    private InventoryLevel createNewInventoryLevel(Location location, ItemVariant itemVariant) {
        InventoryLevel inventoryLevel = new InventoryLevel();
        inventoryLevel.setLocation(location);
        inventoryLevel.setItemVariant(itemVariant);
        inventoryLevel.setQuantityOnHand(BigDecimal.ZERO);
        inventoryLevel.setQuantityReserved(BigDecimal.ZERO);
        inventoryLevel.setReorderPoint(BigDecimal.ZERO);
        inventoryLevel.setReorderQuantity(BigDecimal.ZERO);
        return inventoryLevel;
    }
    
    /**
     * Create an inventory transaction record.
     */
    private InventoryTransaction createInventoryTransaction(Location location, ItemVariant itemVariant,
                                                           InventoryTransaction.TransactionType type,
                                                           BigDecimal quantity, String referenceType,
                                                           String referenceId, String notes, Long userId) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setLocation(location);
        transaction.setItemVariant(itemVariant);
        transaction.setTransactionType(type);
        transaction.setQuantity(quantity);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        transaction.setCreatedByUserId(userId);
        return transactionRepository.save(transaction);
    }
    
    /**
     * Create an empty inventory level DTO.
     */
    private InventoryLevelDto createEmptyInventoryLevelDto(Location location, ItemVariant itemVariant) {
        InventoryLevelDto dto = new InventoryLevelDto();
        dto.setLocationId(location.getId());
        dto.setLocationName(location.getName());
        dto.setItemVariantId(itemVariant.getId());
        dto.setItemVariantSku(itemVariant.getSku());
        dto.setItemVariantName(itemVariant.getVariantName());
        
        if (itemVariant.getItem() != null) {
            dto.setItemName(itemVariant.getItem().getName());
        }
        
        dto.setQuantityOnHand(BigDecimal.ZERO);
        dto.setQuantityReserved(BigDecimal.ZERO);
        dto.setQuantityAvailable(BigDecimal.ZERO);
        dto.setReorderPoint(BigDecimal.ZERO);
        dto.setReorderQuantity(BigDecimal.ZERO);
        
        return dto;
    }
    
    /**
     * Map InventoryLevel entity to InventoryLevelDto.
     */
    private InventoryLevelDto mapToDto(InventoryLevel level) {
        InventoryLevelDto dto = new InventoryLevelDto();
        dto.setId(level.getId());
        
        if (level.getLocation() != null) {
            dto.setLocationId(level.getLocation().getId());
            dto.setLocationName(level.getLocation().getName());
        }
        
        if (level.getItemVariant() != null) {
            dto.setItemVariantId(level.getItemVariant().getId());
            dto.setItemVariantSku(level.getItemVariant().getSku());
            dto.setItemVariantName(level.getItemVariant().getVariantName());
            
            if (level.getItemVariant().getItem() != null) {
                dto.setItemName(level.getItemVariant().getItem().getName());
            }
        }
        
        dto.setQuantityOnHand(level.getQuantityOnHand());
        dto.setQuantityReserved(level.getQuantityReserved());
        dto.setQuantityAvailable(level.getQuantityOnHand().subtract(level.getQuantityReserved()));
        dto.setReorderPoint(level.getReorderPoint());
        dto.setReorderQuantity(level.getReorderQuantity());
        dto.setLastCountedAt(level.getLastCountedAt());
        
        // Set low stock flag
        if (level.getReorderPoint() != null && level.getReorderPoint().compareTo(BigDecimal.ZERO) > 0) {
            dto.setLowStock(level.getQuantityOnHand().compareTo(level.getReorderPoint()) <= 0);
        } else {
            dto.setLowStock(false);
        }
        
        return dto;
    }
    
    /**
     * Map InventoryTransaction entity to InventoryTransactionDto.
     */
    private InventoryTransactionDto mapTransactionToDto(InventoryTransaction transaction) {
        InventoryTransactionDto dto = new InventoryTransactionDto();
        dto.setId(transaction.getId());
        
        if (transaction.getLocation() != null) {
            dto.setLocationId(transaction.getLocation().getId());
            dto.setLocationName(transaction.getLocation().getName());
        }
        
        if (transaction.getItemVariant() != null) {
            dto.setItemVariantId(transaction.getItemVariant().getId());
            dto.setItemVariantSku(transaction.getItemVariant().getSku());
            
            if (transaction.getItemVariant().getItem() != null) {
                dto.setItemName(transaction.getItemVariant().getItem().getName());
            }
        }
        
        dto.setTransactionType(transaction.getTransactionType().toString());
        dto.setQuantity(transaction.getQuantity());
        dto.setReferenceType(transaction.getReferenceType());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setNotes(transaction.getNotes());
        dto.setCreatedByUserId(transaction.getCreatedByUserId());
        dto.setCreatedAt(transaction.getCreatedAt());
        
        return dto;
    }
}
