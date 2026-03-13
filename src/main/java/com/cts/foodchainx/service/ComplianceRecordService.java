package com.cts.foodchainx.service;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.repository.ComplianceRecordRepository;

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
    
    @Auditable(action = "CREATE_COMPLIANCE_RECORD", resource = "COMPLIANCE") // ADD THIS
    public ComplianceRecord createComplianceRecord(ComplianceRecord record) {

        record.setDate(LocalDate.now());
        ComplianceRecord savedRecord = complianceRecordRepository.save(record);
        log.info("Created new Compliance Record with ID: {}", savedRecord.getComplianceId());
        return savedRecord;
    }

    public List<ComplianceRecord> getComplianceByEntity(Long entityId) {

        return complianceRecordRepository.findByEntityId(entityId);
    }
}