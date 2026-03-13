package com.cts.foodchainx.exception;
// 404 - Not Found
public class FarmNotFoundException extends RuntimeException {
    public FarmNotFoundException(Long farmId) {
        super("Farm with ID " + farmId + " not found.");
    }
}