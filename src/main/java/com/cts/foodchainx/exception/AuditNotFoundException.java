package com.cts.foodchainx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a specific Audit record cannot be found in the system.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AuditNotFoundException extends RuntimeException {

    public AuditNotFoundException(Long auditId) {
        super("Audit record not found for ID: " + auditId);
    }

    public AuditNotFoundException(String message) {
        super(message);
    }
}