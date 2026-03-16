package com.cts.foodchainx.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for the FoodChainX platform.
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

    /** * 2. 401 – Authentication Failures (IAM)
     */
    @ExceptionHandler({
        InvalidCredentialsException.class,
        JwtTokenException.class
    })
    public ResponseEntity<Map<String, Object>> handleUnauthorized(RuntimeException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex, false);
    }

    /** * 3. 403 – Account Status / Forbidden (IAM)
     */
    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<Map<String, Object>> handleAccountStatus(AccountStatusException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex, false);
    }

    /** * 4. 409 – Business Conflict (Duplicate User) 
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserExists(UserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex, false);
    }

    /** * 5. 409 – Database Integrity Conflict 
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "A record with the same value already exists.";
        Throwable rootCause = ex.getRootCause();
        String causeMessage = (rootCause != null) ? rootCause.getMessage() : "";

        if (causeMessage != null) {
            if (causeMessage.contains("uk_users_email") || causeMessage.contains("uc_user_email")) {
                msg = "Email address is already registered.";
            } else if (causeMessage.contains("uk_users_phone") || causeMessage.contains("uc_user_phone")) {
                msg = "Phone number is already registered.";
            }
        }
        return build(HttpStatus.CONFLICT, msg, ex, false);
    }

    /** * 6. 400 – Bad Request / Invalid Argument 
     */
    @ExceptionHandler({
        IllegalArgumentException.class,
        InsufficientStockException.class,
        WarehouseCapacityException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request.";
        if (message.toLowerCase().contains("already registered") || message.toLowerCase().contains("duplicate")) {
            return build(HttpStatus.CONFLICT, message, ex, false);
        }
        return build(HttpStatus.BAD_REQUEST, message, ex, false);
    }

    /** * 7. 422 – DTO / Bean Validation Failure 
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.UNPROCESSABLE_ENTITY, details, ex, false);
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
        if (ex instanceof RuntimeException && ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected system error occurred.", ex, true);
    }
}