package com.cts.foodchainx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.ComplianceType;
import com.cts.foodchainx.model.ComplianceRecord;

import java.util.List;

/**
 * Repository interface for {@link ComplianceRecord} entities.
 * <p>
 * Provides an abstraction layer for performing CRUD operations and custom 
 * database queries against the COMPLIANCE_RECORD table. Leverages Spring Data JPA 
 * to generate SQL queries dynamically based on method names.
 * </p>
 */
@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    /**
     * Retrieves all compliance audit entries associated with a specific entity.
     * <p>
     * Used to generate the audit history for a single supply chain participant 
     * regardless of their type (Farmer, Distributor, or Retailer).
     * </p>
     *
     * @param entityId The unique identifier of the entity to query.
     * @return A list of {@link ComplianceRecord} objects belonging to the entity.
     */
    List<ComplianceRecord> findByEntityId(Long entityId);

    /**
     * Finds compliance records based on the actor's organizational category.
     * <p>
     * Useful for aggregating audits specifically for all Farmers, all Distributors, 
     * or all Retailers across the entire system.
     * </p>
     *
     * @param type The {@link ComplianceType} constant to filter by.
     * @return A list of records matching the specified category.
     */
    List<ComplianceRecord> findByType(ComplianceType type);

    /**
     * Filters compliance records by the final outcome of the audit.
     * <p>
     * Primarily utilized by the Regulator role to identify non-compliant entities 
     * or to calculate the overall system pass rate.
     * </p>
     *
     * @param result The {@link ComplianceResult} status (e.g., PASSED, FAILED).
     * @return A list of compliance records matching the outcome.
     */
    List<ComplianceRecord> findByResult(ComplianceResult result);
}