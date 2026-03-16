package com.cts.foodchainx.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for the FoodChainX platform.
 * Standardizes all error responses into a consistent JSON structure.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, Object> body(HttpStatus status, String message, String correlationId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", LocalDateTime.now());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        map.put("correlationId", correlationId);
        return map;
    }

    private ResponseEntity<Map<String, Object>> build(@NonNull HttpStatus status, String message, Throwable ex, boolean serverError) {
        String cid = UUID.randomUUID().toString();
        
        if (serverError) {
            log.error("[{}] {} - Critical Error", cid, message, ex);
        } else {
            log.warn("[{}] {} - Client Error: {}", cid, message, ex.getMessage());
        }

        if (status == HttpStatus.FORBIDDEN) {
            log.warn("[{}] Security Block: {}", cid, message);
        }

        return new ResponseEntity<>(body(status, message, cid), status);
    }

    /** * 1. 404 – Resource Not Found 
     */
    @ExceptionHandler({
        FarmNotFoundException.class,
        BatchNotFoundException.class,
        NotificationNotFoundException.class,
        ComplianceRecordNotFoundException.class,
        ConsumerNotFoundException.class,
        InventoryNotFoundException.class,
        SaleNotFoundException.class,
        AuditNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);
    }

    /** * 2. 409 – Business Conflict (Duplicate User) 
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserExists(UserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex, false);
    }

    /** * 3. 409 – Database Integrity Conflict 
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "A record with the same value already exists.";
        Throwable rootCause = ex.getRootCause();
        String causeMessage = (rootCause != null) ? rootCause.getMessage() : "";

        if (causeMessage != null) {
            if (causeMessage.contains("uk_users_email")) {
                msg = "Email address is already registered.";
            } else if (causeMessage.contains("uk_users_phone")) {
                msg = "Phone number is already registered.";
            }
        }
        return build(HttpStatus.CONFLICT, msg, ex, false);
    }

    /** * 4. 400 – Bad Request / Invalid Argument 
     */
    @ExceptionHandler({
        IllegalArgumentException.class,
        InsufficientStockException.class,
        WarehouseCapacityException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request.";
        
        // Check if the illegal argument is actually a duplicate/conflict case
        if (message.toLowerCase().contains("already registered") || message.toLowerCase().contains("duplicate")) {
            return build(HttpStatus.CONFLICT, message, ex, false);
        }
        
        return build(HttpStatus.BAD_REQUEST, message, ex, false);
    }

    /** * 5. 422 – DTO / Bean Validation Failure 
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        return build(HttpStatus.UNPROCESSABLE_ENTITY, details, ex, false);
    }

    /** * 6. 400 – Malformed JSON structure 
     */
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, Object>> handleBadFormat(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request. Verify JSON structure and field types.", ex, false);
    }

    /** * 7. 400 – Parameter Constraint Violations 
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        
        return build(HttpStatus.BAD_REQUEST, details, ex, false);
    }

    /** * 8. 403 – Security Authorization Failure 
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access Denied: Insufficient permissions.", ex, false);
    }

    /** * 9. 500 – Global Safety Net 
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // Fallback for missing resources caught as general runtime exceptions
        if (ex instanceof RuntimeException && ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);
        }

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected system error occurred.", ex, true);
    }
}