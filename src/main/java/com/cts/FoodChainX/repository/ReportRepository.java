package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    
}
