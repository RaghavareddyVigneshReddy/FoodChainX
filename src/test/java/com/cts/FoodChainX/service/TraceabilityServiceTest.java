package com.cts.foodchainx.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.enums.QualityStatus; // Added Enum
import com.cts.foodchainx.enums.TraceStatus;   // Added Enum
import com.cts.foodchainx.exception.BatchNotFoundException;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.serviceimpl.TraceabilityServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TraceabilityServiceTest {

    @Mock
    private TraceRecordRepository traceRecordRepository;

    @Mock
    private QualityLoggingRepository qualityLoggingRepository;

    @InjectMocks
    private TraceabilityServiceImpl traceabilityService;

    private TraceRecord sampleRecord;
    private ProductionBatch sampleBatch;
    private QualityCheck sampleQuality;
    private final Long BATCH_ID = 101L;

    @BeforeEach
    void setUp() {
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setCropType("Mango");
        sampleBatch.setHarvestDate(LocalDate.now());

        sampleRecord = new TraceRecord();
        sampleRecord.setTraceId(1L);
        sampleRecord.setProductionBatch(sampleBatch);
        // Updated: Using Enum constant
        sampleRecord.setStatus(TraceStatus.HARVESTED); 
        sampleRecord.setDate(LocalDate.now());

        sampleQuality = new QualityCheck();
        // Updated: Using Enum constant
        sampleQuality.setStatus(QualityStatus.PASSED); 
        sampleQuality.setFindings("Grade A");
    }

    @Test
    @DisplayName("Get Traceability Data - Successfully maps Enums to DTO strings")
    void testGetTraceabilityData_Success() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        TraceRecordResponseDto response = traceabilityService.getTraceabilityData(BATCH_ID);

        assertNotNull(response);
        assertEquals(BATCH_ID, response.batchId());
        assertTrue(response.isQualityCertified());
        // Verify the Enum was converted to string name for the DTO
        assertEquals(TraceStatus.HARVESTED.name(), response.status());
        assertEquals("Grade A", response.qualityGrade());
    }

    @Test
    @DisplayName("Generate QR Payload - Correctly formats Enum names in piped string")
    void testGenerateQrPayload_Success() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        String payload = traceabilityService.generateQrPayload(BATCH_ID);

        assertNotNull(payload);
        assertTrue(payload.startsWith("FCX|Batch:101"));
        assertTrue(payload.contains("Cert:true"));
        // Verify payload contains Enum name
        assertTrue(payload.contains("Status:" + TraceStatus.HARVESTED.name()));
        assertTrue(payload.contains("Ret:Local Market"));
    }

    @Test
    @DisplayName("Get Batch History - Returns chronological list of Enum statuses")
    void testGetBatchHistory_Success() {
        TraceRecord secondRecord = new TraceRecord();
        secondRecord.setProductionBatch(sampleBatch);
        secondRecord.setStatus(TraceStatus.IN_TRANSIT); // Updated to Enum

        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord, secondRecord));
        
        List<TraceRecordResponseDto> history = traceabilityService.getBatchHistory(BATCH_ID);

        assertEquals(2, history.size());
        assertEquals(TraceStatus.HARVESTED.name(), history.get(0).status());
        assertEquals(TraceStatus.IN_TRANSIT.name(), history.get(1).status());
    }

    @Test
    @DisplayName("Exception Handling - BatchNotFoundException on empty history")
    void testGetBatchHistory_Empty_ThrowsException() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        assertThrows(BatchNotFoundException.class, () -> {
            traceabilityService.getBatchHistory(BATCH_ID);
        });
    }

    @Test
    @DisplayName("Exception Handling - EntityNotFoundException on QR Payload fail")
    void testGenerateQrPayload_NotFound_ThrowsException() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> {
            traceabilityService.generateQrPayload(BATCH_ID);
        });
    }
}