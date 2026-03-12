package com.cts.foodchainx.dto.audit;

import java.time.Instant;

public record AuditLogResponse(
        Long auditId,
        Long userId,
        String action,
        String resource,
        Instant timestamp
) {}