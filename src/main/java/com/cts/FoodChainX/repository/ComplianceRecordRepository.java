package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.ComplianceRecord;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Integer> {

    List<ComplianceRecord> findByEntityID(Integer entityID);

    List<ComplianceRecord> findByType(String type);

    List<ComplianceRecord> findByResult(String result);
}
