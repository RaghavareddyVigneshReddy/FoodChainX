package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.exception.ComplianceRecordNotFoundException;
import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.repository.ComplianceRecordRepository;
import com.cts.foodchainx.service.ComplianceRecordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for managing compliance records within the FoodChainX system.
 * This class handles the persistence of regulatory audit results and provides
 * retrieval methods for tracking entity compliance history.
 */
@Service
@Slf4j
@RequiredArgsConstructor  
public class ComplianceRecordServiceImpl implements ComplianceRecordService {

    private final ComplianceRecordRepository complianceRecordRepository;

    /**
     * Records a new compliance audit result for a supply chain entity.
     * Automatically assigns the current system date as the audit date.
     * * @param complianceRecord The record containing entity ID, type, and result.
     * @return The persisted {@link ComplianceRecord} entity.
     */
    @Override
    @Auditable(action = "CREATE_COMPLIANCE_RECORD", resource = "COMPLIANCE")
    public ComplianceRecord createComplianceRecord(ComplianceRecord complianceRecord) {
        complianceRecord.setDate(LocalDate.now());
        
        ComplianceRecord savedRecord = complianceRecordRepository.save(complianceRecord);
        log.info("Regulator created a new {} compliance record for Entity: {}", 
                 savedRecord.getType(), savedRecord.getEntityId());
        
        return savedRecord;
    }

    /**
     * Retrieves the complete compliance audit history for a specific actor.
     * * @param entityId The unique identifier of the Farm, Distributor, or Retailer.
     * @return A list of {@link ComplianceRecord} objects for the specified entity.
     */
    @Override
    public List<ComplianceRecord> getHistoryByEntity(Long entityId) {
        List<ComplianceRecord> records = complianceRecordRepository.findByEntityId(entityId);
        
        if (records.isEmpty()) {
            log.warn("No compliance records found for entity ID: {}", entityId);
            throw new ComplianceRecordNotFoundException(entityId);
        }
        return records;
    }

    /**
     * Filters and retrieves all compliance records that resulted in a failure.
     * Primarily used by regulators to identify high-risk participants in the chain.
     * * @return A list of records with a {@link ComplianceResult#FAILED} status.
     */
    @Override
    public List<ComplianceRecord> getFailedRecords() {
        List<ComplianceRecord> failedRecords = complianceRecordRepository.findByResult(ComplianceResult.FAILED);
        
        if (failedRecords.isEmpty()) {
            log.info("Clean audit slate: No failed compliance records found.");
            throw new ComplianceRecordNotFoundException("No failed compliance records found in the system.");
        }
        return failedRecords;
    }
}