package com.cts.foodchainx.service;

import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.ComplianceType;
import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.repository.ComplianceRecordRepository;
import com.cts.foodchainx.serviceimpl.ComplianceRecordServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceRecordServiceTest {

    @Mock
    private ComplianceRecordRepository complianceRecordRepository;

    @InjectMocks
    private ComplianceRecordServiceImpl complianceRecordService;

    private ComplianceRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = ComplianceRecord.builder()
                .complianceId(1L)
                .entityId(500L)
                .type(ComplianceType.FARMER)
                .result(ComplianceResult.PASSED)
                .notes("Sample Note")
                .build();
    }

    @Test
    @DisplayName("Should set current date and save compliance record")
    void createComplianceRecord_Success() {
        // Arrange
        when(complianceRecordRepository.save(any(ComplianceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ComplianceRecord result = complianceRecordService.createComplianceRecord(testRecord);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getDate(), "Date should be set to today");
        verify(complianceRecordRepository, times(1)).save(testRecord);
    }

    @Test
    @DisplayName("Should retrieve history for a specific entity")
    void getHistoryByEntity_Success() {
        // Arrange
        List<ComplianceRecord> history = List.of(testRecord);
        when(complianceRecordRepository.findByEntityId(500L)).thenReturn(history);

        // Act
        List<ComplianceRecord> result = complianceRecordService.getHistoryByEntity(500L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(500L, result.get(0).getEntityId());
        verify(complianceRecordRepository).findByEntityId(500L);
    }

    @Test
    @DisplayName("Should retrieve only failed records")
    void getFailedRecords_Success() {
        // Arrange
        ComplianceRecord failedRecord = new ComplianceRecord();
        failedRecord.setResult(ComplianceResult.FAILED);
        
        when(complianceRecordRepository.findByResult(ComplianceResult.FAILED))
                .thenReturn(List.of(failedRecord));

        // Act
        List<ComplianceRecord> result = complianceRecordService.getFailedRecords();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(ComplianceResult.FAILED, result.get(0).getResult());
        verify(complianceRecordRepository).findByResult(ComplianceResult.FAILED);
    }
}