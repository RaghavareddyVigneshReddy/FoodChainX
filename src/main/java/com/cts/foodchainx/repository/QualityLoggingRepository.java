package com.cts.foodchainx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.model.QualityCheck;

/**
 * Repository interface for {@link QualityCheck} entity.
 * Handles the persistence of inspection logs and provides specialized queries 
 * for status tracking and historical audit trails.
 */
@Repository
public interface QualityLoggingRepository extends JpaRepository<QualityCheck, Long> {

    /**
     * Retrieves all quality inspection logs that match a specific status.
     * <p>The search is case-insensitive, making it robust against varying input 
     * formats (e.g., 'passed' vs 'PASSED').</p>
     * * @param status The result of the inspection (e.g., APPROVED, REJECTED).
     * @return A list of quality checks matching the given status.
     */
    List<QualityCheck> findByStatus(QualityStatus status);

    /**
     * Retrieves the most recent quality check performed on a specific production batch.
     * <p><b>Query Logic:</b>
     * 1. Filters by the Production ID within the nested Batch object.
     * 2. Orders all matches by the inspection date in descending order (Newest first).
     * 3. Limits the result to only the first (latest) record.</p>
     * * @param productionId The ID of the batch to check.
     * @return An Optional containing the latest inspection log, or empty if no checks exist.
     */
    Optional<QualityCheck> findFirstByBatch_ProductionIdOrderByDateDesc(Long productionId);
}
