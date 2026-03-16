package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.audit.AuditLogResponse;
import com.cts.foodchainx.model.User;

import org.springframework.lang.NonNull;
import java.util.List;

/**
 * Service responsible for managing system-wide audit logs.
 * <p>
 * This service provides methods to record new security or business events 
 * and retrieve historical logs for compliance and monitoring purposes.
 * </p>
 */
public interface AuditLogService {

    /**
     * Creates and persists a new audit entry in the database.
     * * @param user     The {@link User} who performed the action.
     * @param action   The operation name (e.g., "USER_LOGIN", "UPDATE_FARM").
     * @param resource The identifier of the resource affected.
     */
    public void log(@NonNull User user, @NonNull String action, @NonNull String resource);

    /**
     * Retrieves all audit logs associated with a specific user.
     * * @param user The user whose activity history is being requested.
     * @return A list of {@link AuditLogResponse} DTOs.
     */
    public List<AuditLogResponse> getLogsForUser(@NonNull User user);

    /**
     * Retrieves all logs across the entire system.
     * Typically used by system administrators or regulators.
     * * @return A complete list of system audit logs.
     */
    public List<AuditLogResponse> getAllLogs();
}