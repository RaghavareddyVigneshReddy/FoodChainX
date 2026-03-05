package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.ComplianceRecord;
import com.cts.FoodChainX.repository.ComplianceRecordRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ComplianceRecordService {

    @Autowired
    private ComplianceRecordRepository complianceRecordRepository;

    public ComplianceRecord createComplianceRecord(ComplianceRecord record) {

        record.setDate(LocalDate.now());

        return complianceRecordRepository.save(record);
    }

    public List<ComplianceRecord> getComplianceByEntity(Integer entityId) {

        return complianceRecordRepository.findByEntityId(entityId);
    }
}