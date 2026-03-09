package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.ComplianceRecord;
import com.cts.FoodChainX.repository.ComplianceRecordRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ComplianceRecordService {

    @Autowired
    private ComplianceRecordRepository complianceRecordRepository;

    public ComplianceRecord createComplianceRecord(ComplianceRecord record) {

        record.setDate(LocalDate.now());
        ComplianceRecord savedRecord = complianceRecordRepository.save(record);
        log.info("Created new Compliance Record with ID: {}", savedRecord.getComplianceId());
        return savedRecord;
    }

    public List<ComplianceRecord> getComplianceByEntity(Integer entityId) {

        return complianceRecordRepository.findByEntityId(entityId);
    }
}