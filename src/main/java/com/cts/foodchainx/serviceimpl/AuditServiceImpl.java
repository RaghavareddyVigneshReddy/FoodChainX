package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.repository.AuditRepository;
import com.cts.foodchainx.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService{

    private final AuditRepository auditRepository;
    @Auditable(action = "INITIATE_AUDIT_RECORD", resource = "AUDIT_PROCESS")
    public Audit createAudit(Audit audit) {
        log.info("Creating new Audit record for Entity: {}", audit.getAuditId());
        audit.setDate(LocalDate.now());
        audit.setStatus("OPEN");
        Audit savedAudit = auditRepository.save(audit);
        log.debug("Audit record saved with ID: {}", savedAudit.getAuditId());
        return savedAudit;
    }

    @Auditable(action = "CLOSE_AUDIT_RECORD", resource = "AUDIT_PROCESS")
    public Audit closeAudit(@NonNull Long auditId) {

        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        audit.setStatus("CLOSED");

        return auditRepository.save(audit);
    }
}