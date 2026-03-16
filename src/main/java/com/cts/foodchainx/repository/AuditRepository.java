package com.cts.foodchainx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.enums.AuditStatus;
import com.cts.foodchainx.model.Audit;

import java.util.List;

/**
 * Repository interface for {@link Audit} entities.
 * <p>
 * This interface handles the persistence logic for formal regulatory audits. 
 * It allows the system to track the lifecycle of an audit from initiation 
 * (OPEN) to completion (CLOSED), providing oversight capabilities for the 
 * administrative and regulatory modules.
 * </p>
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    /**
     * Retrieves all formal audits initiated by a specific regulator.
     * <p>
     * This is primarily used to populate the "My Audits" view for users 
     * with the REGULATOR role, allowing them to manage their own assigned tasks.
     * </p>
     *
     * @param regulatorId The unique identifier of the regulator.
     * @return A list of {@link Audit} records managed by the specified regulator.
     */
    List<Audit> findByRegulatorId(Long regulatorId);

    /**
     * Filters audits based on their current lifecycle stage.
     * <p>
     * Often used to identify "OPEN" audits that require immediate attention 
     * or "CLOSED" audits for historical reporting and archiving.
     * </p>
     *
     * @param status The {@link AuditStatus} (e.g., OPEN, CLOSED) to filter by.
     * @return A list of audits matching the specified status.
     */
    List<Audit> findByStatus(AuditStatus status);

    /**
     * Finds audits associated with a specific organizational or regional scope.
     * <p>
     * Helps in tracking audits performed under specific themes or timeframes, 
     * such as "FARMER_SAFETY_2026" or "DISTRICT_A_RETAIL_REVIEW".
     * </p>
     *
     * @param scope The string-based scope or name of the audit campaign.
     * @return A list of audits belonging to the defined scope.
     */
    List<Audit> findByScope(String scope);
}