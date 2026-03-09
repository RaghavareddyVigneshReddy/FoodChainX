package com.cts.FoodChainX.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> body(HttpStatus status, String message, String correlationId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", LocalDateTime.now());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        map.put("correlationId", correlationId);
        return map;
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Throwable ex, boolean serverError) {
        String cid = UUID.randomUUID().toString();
        if (serverError) {
            log.error("[{}] {}", cid, message, ex);
        } else {
            log.warn("[{}] {} - {}", cid, message, ex.getMessage());
        }
        return new ResponseEntity<>(body(status, message, cid), status);
    }

    /** 404 – domain not-found (e.g., notifications) */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotificationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);
    }

    /** 409 – duplicate user (email/phone) */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserExists(UserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex, false);
    }

    /**
     * 409 – DB unique-index conflict (e.g., uk_users_email / uk_users_phone).
     * If you name your constraints, you can tailor messages here.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "Unique constraint violated. A record with the same value already exists.";
        String cause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        if (cause != null) {
            if (cause.contains("uk_users_email")) {
                msg = "Email already registered";
            } else if (cause.contains("uk_users_phone")) {
                msg = "Phone number already registered";
            }
        }
        return build(HttpStatus.CONFLICT, msg, ex, false);
    }

    /**
     * 400 – bad input or business rule violations sent as IllegalArgumentException.
     * If you prefer 409 for certain messages, you can detect them here.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        // Optionally route known conflict messages to 409:
        if (message.toLowerCase().contains("already registered") || message.toLowerCase().contains("duplicate")) {
            return build(HttpStatus.CONFLICT, message, ex, false);
        }
        return build(HttpStatus.BAD_REQUEST, message, ex, false);
    }

    /** 422 – bean validation (for @Valid request bodies) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.UNPROCESSABLE_ENTITY, details, ex, false);
    }

    /** 400 – invalid JSON, malformed body, wrong enum value in body, etc. */
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, Object>> handleBadFormat(Exception ex) {
        String msg = "Malformed request. Please verify JSON structure and field types.";
        return build(HttpStatus.BAD_REQUEST, msg, ex, false);
    }

    /** 400 – constraint violations on query/path params with @Validated */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, details, ex, false);
    }

    /** 500 – safety net */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex, true);
    }


        /** 404 – record not found (like the "Batch not found" from your service) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // If the message contains "not found", return 404, otherwise 400
        HttpStatus status = ex.getMessage().toLowerCase().contains("not found") 
                            ? HttpStatus.NOT_FOUND 
                            : HttpStatus.BAD_REQUEST;
        
        return build(status, ex.getMessage(), ex, false);
    }

    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBatchNotFound(BatchNotFoundException ex) {
        // We use HttpStatus.NOT_FOUND (404) because the resource doesn't exist.
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);
    }
}