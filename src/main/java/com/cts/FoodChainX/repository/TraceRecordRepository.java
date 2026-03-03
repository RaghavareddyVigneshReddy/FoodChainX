package com.cts.FoodChainX.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.TraceRecord;

@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, Long> {
    // Corrected to match ProductionBatch.productionId
    Optional<TraceRecord> findByProductionBatch_ProductionId(Long productionId);
}