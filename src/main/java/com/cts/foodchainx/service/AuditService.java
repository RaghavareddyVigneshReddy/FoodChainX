package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Audit;
import org.springframework.lang.NonNull;

public interface AuditService {
    Audit createAudit(Audit audit);
    Audit closeAudit(@NonNull Long auditId);
}