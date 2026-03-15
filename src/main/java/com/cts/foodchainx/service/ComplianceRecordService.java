package com.cts.foodchainx.service;

import com.cts.foodchainx.model.ComplianceRecord;
import java.util.List;

/**
 * Service interface for managing regulatory compliance across the food supply chain.
 * <p>
 * This service provides the core functionality for the Regulator Workbench, 
 * allowing for the creation of audit results and the retrieval of compliance 
 * histories for various supply chain entities (Farms, Distributors, and Retailers).
 * </p>
 */
public interface ComplianceRecordService {

    /**
     * Persists a new compliance audit result for a specific supply chain participant.
     * <p>
     * This method records whether an entity has passed or failed its most recent 
     * regulatory inspection. The record is used to determine the "Certified" status 
     * displayed in the Consumer Portal.
     * </p>
     *
     * @param complianceRecord The {@link ComplianceRecord} entity containing entity details, 
     * type, and audit results.
     * @return The saved {@link ComplianceRecord} entity with an assigned ID and timestamp.
     */
    public ComplianceRecord createComplianceRecord(ComplianceRecord complianceRecord);

    /**
     * Retrieves the historical list of compliance audits for a specific actor.
     * <p>
     * Used to build a transparency profile for a Farm, Distributor, or Retailer, 
     * showing their track record of regulatory adherence over time.
     * </p>
     *
     * @param entityId The unique identifier of the supply chain actor.
     * @return A {@link List} of {@link ComplianceRecord} objects associated with the entity.
     */
    public List<ComplianceRecord> getHistoryByEntity(Long entityId);

    /**
     * Identifies all supply chain participants that have failed their most recent inspections.
     * <p>
     * This helper method allows Regulators to quickly identify high-risk entities 
     * that may require immediate intervention or suspension from the supply chain.
     * </p>
     *
     * @return A {@link List} of all {@link ComplianceRecord} entries with a FAILED status.
     */
    public List<ComplianceRecord> getFailedRecords();
}