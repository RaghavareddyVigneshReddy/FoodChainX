package com.cts.foodchainx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a compliance record or history cannot be located.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ComplianceRecordNotFoundException extends RuntimeException {

    public ComplianceRecordNotFoundException(String message) {
        super(message);
    }

    public ComplianceRecordNotFoundException(Long entityId) {
        super("No compliance history found for Entity ID: " + entityId);
    }
}