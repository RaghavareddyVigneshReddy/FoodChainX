package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.TraceRecord;

/**
 * Repository interface for {@link TraceRecord} entities.
 * provides abstraction for database operations related to tracking the movement
 * and status of production batches across the FoodChainX supply chain.
 */
@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, Long> {

    /**
     * Retrieves a complete list of traceability records for a specific production batch.
     * The results are sorted primarily by date in descending order, and secondarily by 
     * Trace ID in descending order to ensure the most recent events appear first.
     *
     * @param productionId the unique ID of the {@link com.cts.FoodChainX.model.ProductionBatch}
     * @return a {@link List} of {@link TraceRecord} objects representing the batch's journey
     */
    List<TraceRecord> findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(Long productionId);
}