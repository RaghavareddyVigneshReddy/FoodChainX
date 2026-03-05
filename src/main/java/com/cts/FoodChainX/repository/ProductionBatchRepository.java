package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.ProductionBatch;
@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
/**
     * Finds all batches linked to a specific Farm.
     * Use findByFarm_Id if your Farm entity uses "private Long id" 
     * or findByFarm_FarmId if it uses "private Long farmId".
     */
    List<ProductionBatch> findByFarm_FarmId(Long farmId);

    /**
     * Optional: Useful for the Quality Module to see what needs checking.
     */
    List<ProductionBatch> findByQualityStatus(String status);



}