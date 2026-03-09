package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.Report; // Ensure you have a Report entity matching source 92
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByScopeIgnoreCase(String scope);
}