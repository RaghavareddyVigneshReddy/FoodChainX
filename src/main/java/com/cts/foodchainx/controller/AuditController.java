package com.cts.foodchainx.controller;

import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.service.AuditService;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing high-level Regulatory Audits.
 * <p>
 * This controller facilitates the formal audit lifecycle in the FoodChainX platform.
 * It provides endpoints for Regulators to initiate broad-scope audits and formally
 * conclude them once all compliance checks for the period are finished.
 * </p>
 */
@RestController
@RequestMapping("/api/compliance/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * Initiates a new formal audit process.
     * <p><b>Endpoint:</b> POST /api/compliance/audits</p>
     * <p>
     * Typically used by a Regulator to start a thematic or regional inspection 
     * (e.g., "Quarterly Farm Safety Review"). The audit will remain in an 
     * OPEN status until manually closed.
     * </p>
     *
     * @param audit The audit entity details provided in the request body.
     * @return The newly created {@link Audit} record with status set to OPEN.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public Audit createAudit(@RequestBody Audit audit) {
        return auditService.createAudit(audit);
    }

    /**
     * Formally closes an ongoing audit by its unique identifier.
     * <p><b>Endpoint:</b> PUT /api/compliance/audits/{id}/close</p>
     * <p>
     * This operation transitions the audit status to CLOSED. Once closed, the audit 
     * findings are finalized for reporting and archival purposes.
     * </p>
     *
     * @param id The unique database ID of the audit to be finalized. Must not be null.
     * @return The updated {@link Audit} entity reflecting the CLOSED status.
     * @throws jakarta.persistence.EntityNotFoundException if the specified audit ID does not exist.
     */
    @PutMapping("/{id}/close")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public Audit closeAudit(@PathVariable @NonNull Long id) {
        return auditService.closeAudit(id);
    }
}