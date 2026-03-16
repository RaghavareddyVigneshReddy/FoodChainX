package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.enums.AuditStatus;
import com.cts.foodchainx.exception.AuditNotFoundException;
import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.repository.AuditRepository;
import com.cts.foodchainx.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service implementation for managing the lifecycle of regulatory audits.
 * <p>
 * This class provides the business logic for initiating and concluding 
 * formal supply chain inspections. It leverages the {@link Auditable} aspect 
 * to ensure all administrative changes are tracked in the system's security logs.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    /**
     * Initiates a new audit process and persists it to the database.
     * <p>
     * Implementation details:
     * 1. Sets the audit date to the current system date.
     * 2. Initializes the status to {@link AuditStatus#OPEN}.
     * 3. Triggers an "INITIATE_AUDIT_RECORD" entry in the global audit logs.
     * </p>
     * * @param audit The audit details provided by the regulator.
     * @return The saved {@link Audit} entity with its generated primary key.
     */
    @Override
    @Auditable(action = "INITIATE_AUDIT_RECORD", resource = "AUDIT_PROCESS")
    public Audit createAudit(Audit audit) {
        log.info("Creating new Audit record for Scope: {}", audit.getScope());
        audit.setDate(LocalDate.now());
        audit.setStatus(AuditStatus.OPEN);
        Audit savedAudit = auditRepository.save(audit);
        log.debug("Audit record saved with ID: {}", savedAudit.getAuditId());
        return savedAudit;
    }

    /**
     * Finalizes an existing audit by updating its status to CLOSED.
     * <p>
     * This method retrieves the audit by its identifier and performs a 
     * state transition to {@link AuditStatus#CLOSED}. This action is 
     * immutable and recorded in the system audit logs.
     * </p>
     * * @param auditId The unique ID of the audit to finalize.
     * @return The updated {@link Audit} entity.
     * @throws RuntimeException if no audit record is found for the given ID.
     */
    @Override
    @Auditable(action = "CLOSE_AUDIT_RECORD", resource = "AUDIT_PROCESS")
    public Audit closeAudit(@NonNull Long auditId) {
        log.info("Closing Audit record with ID: {}", auditId);
        
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new AuditNotFoundException(auditId));

        audit.setStatus(AuditStatus.CLOSED);

        return auditRepository.save(audit);
    }
}