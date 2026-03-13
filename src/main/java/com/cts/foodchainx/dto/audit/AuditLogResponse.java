package com.cts.foodchainx.dto.audit;

import java.time.Instant;

<<<<<<< HEAD
=======
/**
 * Data Transfer Object representing a system audit log entry.
 * * @param auditId   The unique identifier of the audit log record.
 * @param userId    The ID of the user who performed the action.
 * @param action    The type of operation performed (e.g., "CREATE_FARM").
 * @param resource  The specific entity or resource affected.
 * @param timestamp The exact moment the event occurred.
 */
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
public record AuditLogResponse(
        Long auditId,
        Long userId,
        String action,
        String resource,
        Instant timestamp
) {}