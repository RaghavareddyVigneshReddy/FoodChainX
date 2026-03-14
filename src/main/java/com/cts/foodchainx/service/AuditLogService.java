package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.audit.AuditLogResponse;
import com.cts.foodchainx.model.AuditLog;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service responsible for managing system-wide audit logs.
 * <p>
 * This service provides methods to record new security or business events 
 * and retrieve historical logs for compliance and monitoring purposes.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Creates and persists a new audit entry in the database.
     * * @param user     The {@link User} who performed the action.
     * @param action   The operation name (e.g., "USER_LOGIN", "UPDATE_FARM").
     * @param resource The identifier of the resource affected.
     */
    public void log(@NonNull User user, @NonNull String action, @NonNull String resource) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .timestamp(Instant.now())
                .build();
                
        auditLogRepository.save(java.util.Objects.requireNonNull(entry));
    }

    /**
     * Retrieves all audit logs associated with a specific user.
     * * @param user The user whose activity history is being requested.
     * @return A list of {@link AuditLogResponse} DTOs.
     */
    public List<AuditLogResponse> getLogsForUser(@NonNull User user) {
        return auditLogRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves all logs across the entire system.
     * Typically used by system administrators or regulators.
     * * @return A complete list of system audit logs.
     */
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Helper method to transform an {@link AuditLog} entity into a {@link AuditLogResponse}.
     */
    private AuditLogResponse mapToDto(AuditLog log) {
        return new AuditLogResponse(
                log.getAuditId(),
                log.getUser().getUserId(),
                log.getAction(),
                log.getResource(),
                log.getTimestamp()
        );
    }
}