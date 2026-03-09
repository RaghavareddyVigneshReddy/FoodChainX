package com.cts.FoodChainX.exception;

// Custom exceptions should extend RuntimeException
public class BatchNotFoundException extends RuntimeException {
    public BatchNotFoundException(Long batchId) {
        
        super("Trace history for Batch ID " + batchId + " not found.");
    }
}