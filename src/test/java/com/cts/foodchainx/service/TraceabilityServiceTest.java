package com.cts.foodchainx.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.exception.BatchNotFoundException;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
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

/**
 * Unit tests for {@link TraceabilityService}.
 * These tests verify the business logic for product traceability data retrieval,
 * QR code payload generation, and batch journey history.
 * * Uses Mockito to mock repository layers for isolated service testing.
 */
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

    /**
     * Initializes common test data before each test execution.
     * Sets up a sample production batch, a corresponding trace record,
     * and a passed quality check report.
     */
    @BeforeEach
    void setUp() {
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setCropType("Mango");
        sampleBatch.setHarvestDate(LocalDate.now());

        sampleRecord = new TraceRecord();
        sampleRecord.setTraceId(1L);
        sampleRecord.setProductionBatch(sampleBatch);
        sampleRecord.setStatus("HARVESTED_AT_FARM");
        sampleRecord.setDate(LocalDate.now());

        sampleQuality = new QualityCheck();
        sampleQuality.setStatus("PASSED");
        sampleQuality.setFindings("Grade A");
    }

    /**
     * Tests that traceability data is correctly retrieved and mapped to a DTO.
     * Verifies that quality information is correctly merged and fallback strings 
     * (like "N/A" for missing farms) are applied.
     */
    @Test
    void testGetTraceabilityData_Success() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        TraceRecordResponseDto response = traceabilityService.getTraceabilityData(BATCH_ID);

        assertNotNull(response);
        assertEquals(BATCH_ID, response.batchId());
        assertEquals("Mango", response.cropType());
        assertTrue(response.isQualityCertified());
        assertEquals("Grade A", response.qualityGrade());
        assertEquals("N/A", response.farmName());
    }

    /**
     * Verifies that the QR payload generator creates a correctly formatted 
     * pipe-delimited string (FCX|...).
     * Validates that batch details and fallback roles (Retailer/Distributor) are accurate.
     */
    @Test
    void testGenerateQrPayload_Success() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord));
        when(qualityLoggingRepository.findFirstByBatch_ProductionIdOrderByDateDesc(BATCH_ID))
                .thenReturn(Optional.of(sampleQuality));

        String payload = traceabilityService.generateQrPayload(BATCH_ID);

        assertNotNull(payload);
        assertTrue(payload.startsWith("FCX|Batch:101"));
        assertTrue(payload.contains("Cert:true"));
        assertTrue(payload.contains("Status:HARVESTED_AT_FARM"));
        assertTrue(payload.contains("Ret:Local Market"));
    }

    /**
     * Tests the retrieval of the full chronological history for a batch.
     * Verifies that multiple records are returned and the repository is called correctly.
     */
    @Test
    void testGetBatchHistory_Success() {
        TraceRecord secondRecord = new TraceRecord();
        secondRecord.setProductionBatch(sampleBatch);
        secondRecord.setStatus("IN_TRANSIT");

        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(sampleRecord, secondRecord));
        
        List<TraceRecordResponseDto> history = traceabilityService.getBatchHistory(BATCH_ID);

        assertEquals(2, history.size());
        assertEquals("HARVESTED_AT_FARM", history.get(0).status());
        verify(traceRecordRepository, times(1)).findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID);
    }

    /**
     * Verifies that {@link BatchNotFoundException} is thrown when requesting 
     * history for a non-existent batch.
     */
    @Test
    void testGetBatchHistory_Empty_ThrowsException() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        assertThrows(BatchNotFoundException.class, () -> {
            traceabilityService.getBatchHistory(BATCH_ID);
        });
    }

    /**
     * Verifies that {@link EntityNotFoundException} is thrown during QR payload 
     * generation if no trace history exists for the batch.
     */
    @Test
    void testGenerateQrPayload_NotFound_ThrowsException() {
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> {
            traceabilityService.generateQrPayload(BATCH_ID);
        });
    }
}