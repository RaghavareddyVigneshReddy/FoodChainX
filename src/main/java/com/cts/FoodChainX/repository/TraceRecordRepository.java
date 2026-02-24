package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.TraceRecord;

public interface TraceRecordRepository extends JpaRepository<Report, Integer> {
    
}
