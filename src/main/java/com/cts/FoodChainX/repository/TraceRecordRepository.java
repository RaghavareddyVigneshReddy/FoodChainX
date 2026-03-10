package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.TraceRecord;

@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, Long> {
    // To retrieve all trace records for a given production batch, ordered by date descending
    List<TraceRecord> findByProductionBatch_ProductionIdOrderByDateDesc(Long productionId);
}