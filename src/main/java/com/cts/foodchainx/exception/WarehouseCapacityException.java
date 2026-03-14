package com.cts.foodchainx.exception;

/**
 * Custom exception thrown when a warehouse operation fails due to capacity constraints.
 * This helps distinguish business logic failures from standard runtime errors.
 */
public class WarehouseCapacityException extends RuntimeException {
    
    /**
     * Constructs a new WarehouseCapacityException with a specific message.
     * @param message The detailed error message explaining the capacity issue.
     */
    public WarehouseCapacityException(String message) {
        super(message);
    }
}