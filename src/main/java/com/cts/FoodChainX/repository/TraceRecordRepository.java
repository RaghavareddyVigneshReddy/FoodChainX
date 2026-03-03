package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.TraceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, Integer> {
    // Corrected to match ProductionBatch.productionId
    Optional<TraceRecord> findByProductionBatch_ProductionId(Integer productionId);
}