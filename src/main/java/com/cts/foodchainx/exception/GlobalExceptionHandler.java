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

            log.error("[{}] {}", cid, message, ex);

        } else {

            log.warn("[{}] {} - {}", cid, message, ex.getMessage());

        }

        if (status == HttpStatus.FORBIDDEN) {

            log.warn("[{}] Security Block: {}", cid, message);

        }

        return new ResponseEntity<>(body(status, message, cid), status);

    }

    /** * 1. 404 – Handle all specific "Not Found" exceptions here.

     */

    @ExceptionHandler({

        FarmNotFoundException.class,

        BatchNotFoundException.class,

        NotificationNotFoundException.class

    })

    public ResponseEntity<Map<String, Object>> handleAllNotFoundExceptions(RuntimeException ex) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);

    }

    /** 2. 409 – duplicate user (email/phone) */

    @ExceptionHandler(UserAlreadyExistsException.class)

    public ResponseEntity<Map<String, Object>> handleUserExists(UserAlreadyExistsException ex) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), ex, false);

    }

    /** 3. 409 – DB unique-index conflict */

    @ExceptionHandler(DataIntegrityViolationException.class)

    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {

        String msg = "Unique constraint violated. A record with the same value already exists.";
        Throwable rootCause = ex.getRootCause();
        String cause = (rootCause != null) ? rootCause.getMessage() : "";

        if (cause != null) {

            if (cause.contains("uk_users_email")) {

                msg = "Email already registered";

            } else if (cause.contains("uk_users_phone")) {

                msg = "Phone number already registered";

            }

        }

        return build(HttpStatus.CONFLICT, msg, ex, false);

    }

    /** 4. 400/409 – IllegalArgument handling */

    @ExceptionHandler(IllegalArgumentException.class)

    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";

        if (message.toLowerCase().contains("already registered") || message.toLowerCase().contains("duplicate")) {

            return build(HttpStatus.CONFLICT, message, ex, false);

        }

        return build(HttpStatus.BAD_REQUEST, message, ex, false);

    }

    /** 5. 422 – bean validation */

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        String details = ex.getBindingResult().getFieldErrors().stream()

                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())

                .collect(Collectors.joining("; "));

        return build(HttpStatus.UNPROCESSABLE_ENTITY, details, ex, false);

    }

    /** 6. 400 – malformed JSON/Request */

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })

    public ResponseEntity<Map<String, Object>> handleBadFormat(Exception ex) {

        String msg = "Malformed request. Please verify JSON structure and field types.";

        return build(HttpStatus.BAD_REQUEST, msg, ex, false);

    }

    /** 7. 400 – Constraint violations on params */

    @ExceptionHandler(ConstraintViolationException.class)

    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {

        String details = ex.getConstraintViolations().stream()

                .map(v -> v.getPropertyPath() + " " + v.getMessage())

                .collect(Collectors.joining("; "));

        return build(HttpStatus.BAD_REQUEST, details, ex, false);

    }

    /** 8. 403 – Forbidden (Security Authorization failure) */

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)

    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {

        String msg = "Access Denied: You do not have permission to access this resource.";

        return build(HttpStatus.FORBIDDEN, msg, ex, false);

    }

    /** * 9. 500 – Safety Net

     */

    @ExceptionHandler(Exception.class)

    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {

        // Fallback check for missing resources within generic runtime exceptions

        if (ex instanceof RuntimeException && ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {

            return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex, false);

        }

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), ex, true);

    }

}
 