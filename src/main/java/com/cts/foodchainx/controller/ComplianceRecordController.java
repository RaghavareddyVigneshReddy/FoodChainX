package com.cts.foodchainx.controller;

import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.service.ComplianceRecordService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Regulatory Compliance Records.
 * <p>
 * This controller provides the entry points for the Compliance & Audit Management module.
 * It allows Regulators to log audit results and retrieve compliance histories for 
 * various supply chain participants, ensuring transparency and safety standards.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compliance")
public class ComplianceRecordController {

    private final ComplianceRecordService complianceRecordService;

    /**
     * Creates and persists a new compliance audit record.
     * <p><b>Endpoint:</b> POST /api/compliance/records</p>
     * * @param complianceRecord The compliance record details provided in the request body.
     * @return The saved {@link ComplianceRecord} entity including the generated ID and timestamp.
     */
    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public ComplianceRecord createComplianceRecord(@RequestBody ComplianceRecord complianceRecord) {
        return complianceRecordService.createComplianceRecord(complianceRecord);
    }

    /**
     * Retrieves the complete compliance audit history for a specific supply chain entity.
     * <p><b>Endpoint:</b> GET /api/compliance/history/{entityId}</p>
     * * @param entityId The unique identifier of the Farm, Distributor, or Retailer.
     * @return A list of {@link ComplianceRecord} objects associated with the given entity ID.
     */
    @GetMapping("/history/{entityId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ComplianceRecord> getComplianceHistory(@PathVariable Long entityId) {
        return complianceRecordService.getHistoryByEntity(entityId);
    }

    /**
     * Fetches a list of all supply chain entities that have failed their recent compliance audits.
     * <p><b>Endpoint:</b> GET /api/compliance/failed</p>
     * <p>
     * This endpoint is utilized by the Regulator dashboard to quickly identify 
     * non-compliant participants who may require further investigation or suspension.
     * </p>
     * * @return A list of {@link ComplianceRecord} entries with a FAILED status.
     */
    @GetMapping("/failed")
    @ResponseStatus(HttpStatus.OK)
    public List<ComplianceRecord> getFailedComplianceRecords() {
        return complianceRecordService.getFailedRecords();
    }
}