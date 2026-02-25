package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.ProductionBatch;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Integer> {

}