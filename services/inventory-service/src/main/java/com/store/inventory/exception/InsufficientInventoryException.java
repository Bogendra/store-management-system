package com.store.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

/**
 * Exception thrown when there is not enough inventory available for an operation.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String sku, String locationName, BigDecimal availableQuantity, BigDecimal requestedQuantity) {
        super(String.format("Insufficient inventory for item %s at location %s. Available: %s, Requested: %s", 
                sku, locationName, availableQuantity, requestedQuantity));
    }
}
