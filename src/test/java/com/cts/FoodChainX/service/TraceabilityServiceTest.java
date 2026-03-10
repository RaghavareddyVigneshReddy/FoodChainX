package com.cts.FoodChainX.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponseDto;
import com.cts.FoodChainX.exception.BatchNotFoundException;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
    private TraceabilityService traceabilityService;

    private TraceRecord sampleRecord;
    private ProductionBatch sampleBatch;
    private QualityCheck sampleQuality;
    private final Long BATCH_ID = 101L;

    @BeforeEach
    void setUp() {
        // Core dependency: Production Batch
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setCropType("Mango");
        sampleBatch.setHarvestDate(LocalDate.now());

        // The Trace Entry
        sampleRecord = new TraceRecord();
        sampleRecord.setTraceId(1L);
        sampleRecord.setProductionBatch(sampleBatch);
        sampleRecord.setStatus("HARVESTED_AT_FARM");
        sampleRecord.setDate(LocalDate.now());

        // Quality Report
        sampleQuality = new QualityCheck();
        sampleQuality.setStatus("PASSED");
        sampleQuality.setFindings("Grade A");
    }

    @Test
    void testGetTraceabilityData_Success() {
        // Arrange: Mock the list-based repo call and the quality check
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        // Act
        TraceRecordResponseDto response = traceabilityService.getTraceabilityData(BATCH_ID);

        // Assert: Use record accessor methods (batchId(), cropType(), etc.)
        assertNotNull(response);
        assertEquals(BATCH_ID, response.batchId());
        assertEquals("Mango", response.cropType());
        assertTrue(response.isQualityCertified());
        assertEquals("Grade A", response.qualityGrade());
        assertEquals("N/A", response.farmName()); // Testing your "N/A" fallback logic
    }

    @Test
    void testGenerateQrPayload_Success() {
        // Arrange
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        // Act
        String payload = traceabilityService.generateQrPayload(BATCH_ID);

        // Assert: Verify the pipe-delimited format
        assertNotNull(payload);
        assertTrue(payload.startsWith("FCX|Batch:101"));
        assertTrue(payload.contains("Cert:true"));
        assertTrue(payload.contains("Status:HARVESTED_AT_FARM"));
        assertTrue(payload.contains("Ret:Local Market")); // Testing retailer fallback
    }

    @Test
    void testGetBatchHistory_Success() {
        // Arrange: Mocking a timeline with two events
        TraceRecord secondRecord = new TraceRecord();
        secondRecord.setProductionBatch(sampleBatch);
        secondRecord.setStatus("IN_TRANSIT");

        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord, secondRecord));
        
        // Act
        List<TraceRecordResponseDto> history = traceabilityService.getBatchHistory(BATCH_ID);

        // Assert
        assertEquals(2, history.size());
        assertEquals("HARVESTED_AT_FARM", history.get(0).status());
        verify(traceRecordRepository, times(1)).findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID);
    }

    @Test
    void testGetBatchHistory_Empty_ThrowsException() {
        // Arrange
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BatchNotFoundException.class, () -> {
            traceabilityService.getBatchHistory(BATCH_ID);
        });
    }

    @Test
    void testGenerateQrPayload_NotFound_ThrowsException() {
        // Arrange
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            traceabilityService.generateQrPayload(BATCH_ID);
        });
    }
}