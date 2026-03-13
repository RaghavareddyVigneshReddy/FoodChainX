package com.cts.foodchainx.dto.audit;

import java.time.Instant;

/**
 * Data Transfer Object representing a system audit log entry.
 * * @param auditId   The unique identifier of the audit log record.
 * @param userId    The ID of the user who performed the action.
 * @param action    The type of operation performed (e.g., "CREATE_FARM").
 * @param resource  The specific entity or resource affected.
 * @param timestamp The exact moment the event occurred.
 */
public record AuditLogResponse(
        Long auditId,
        Long userId,
        String action,
        String resource,
        Instant timestamp
) {}