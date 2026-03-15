package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Audit;
import org.springframework.lang.NonNull;

/**
 * Service interface for managing high-level regulatory audits.
 * <p>
 * This service handles the formal lifecycle of a supply chain audit, providing 
 * the administrative structure for regulators to oversee production, 
 * distribution, and retail operations. It ensures that every audit is 
 * properly initiated and formally concluded.
 * </p>
 */
public interface AuditService {

    /**
     * Initiates a new formal audit record in the system.
     * <p>
     * This method is called by a Regulator to define the scope and initial 
     * objectives of an inspection. The audit status is typically set to 'OPEN' 
     * upon creation to allow for ongoing compliance record logging.
     * </p>
     *
     * @param audit The {@link Audit} entity containing regulator ID, scope, and initial findings.
     * @return The persisted {@link Audit} entity with a generated ID and timestamp.
     */
    Audit createAudit(Audit audit);

    /**
     * Finalizes an ongoing audit and marks it as complete.
     * <p>
     * Concluding an audit transition the status to 'CLOSED', making the 
     * findings immutable. This process is essential for generating historical 
     * compliance reports and ensuring regulatory accountability.
     * </p>
     *
     * @param auditId The unique identifier of the audit to be closed. Must not be null.
     * @return The updated {@link Audit} entity reflecting the CLOSED status.
     * @throws jakarta.persistence.EntityNotFoundException if no audit exists with the given ID.
     */
    Audit closeAudit(@NonNull Long auditId);
}