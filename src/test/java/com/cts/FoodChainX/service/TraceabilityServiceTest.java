package com.cts.FoodChainX.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponseDto;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TraceabilityServiceTest {

    @Mock
    private TraceRecordRepository traceRecordRepository;

    @InjectMocks
    private TraceabilityService traceabilityService;

    private TraceRecord sampleRecord;
    private ProductionBatch sampleBatch;
    private final Long BATCH_ID = 101L;

    @BeforeEach
    void setUp() {
        // Initialize the ProductionBatch (The core dependency)
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setCropType("Organic Basmati");

        // Initialize the TraceRecord
        sampleRecord = new TraceRecord();
        sampleRecord.setTraceId(1L);
        sampleRecord.setProductionBatch(sampleBatch);
        sampleRecord.setStatus("PROCESSING");
        sampleRecord.setDate(LocalDate.now());
    }

    @Test
    void testGetTraceabilityData_Success_WithFullData() {
        // 1. Arrange: Mock a record with a Farm and a Consumer
        Farm farm = new Farm();
        farm.setName("Naveen's Estate");
        sampleRecord.setFarm(farm);

        User consumer = new User();
        consumer.setName("Johnny");
        sampleRecord.setConsumer(consumer);

        when(traceRecordRepository.findByProductionBatch_ProductionId(BATCH_ID))
                .thenReturn(Optional.of(sampleRecord));

        // 2. Act
        TraceRecordResponseDto response = traceabilityService.getTraceabilityData(BATCH_ID);

        // 3. Assert: Using Record accessors (no 'get' prefix)
        assertNotNull(response);
        assertEquals("Organic Basmati", response.cropType());
        assertEquals("Naveen's Estate", response.farmName());
        assertEquals("Johnny", response.consumerName());
        assertEquals("In Transit", response.distributorName()); // Since distributor is null
        verify(traceRecordRepository).findByProductionBatch_ProductionId(BATCH_ID);
    }

    @Test
    void testGetTraceabilityData_Success_WithNullRelationships() {
        // Arrange: record has null Farm, Distributor, and Consumer (set in @BeforeEach)
        when(traceRecordRepository.findByProductionBatch_ProductionId(BATCH_ID))
                .thenReturn(Optional.of(sampleRecord));

        // Act
        TraceRecordResponseDto response = traceabilityService.getTraceabilityData(BATCH_ID);

        // Assert: Verify your service's "Default Strings" logic
        assertEquals("N/A", response.farmName());
        assertEquals("In Transit", response.distributorName());
        assertEquals("Not Yet Purchased", response.consumerName());
    }

    @Test
    void testGetTraceabilityData_NotFound_ThrowsException() {
        // Arrange
        when(traceRecordRepository.findByProductionBatch_ProductionId(BATCH_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            traceabilityService.getTraceabilityData(BATCH_ID);
        });

        assertTrue(exception.getMessage().contains("Batch ID " + BATCH_ID));
    }

    @Test
    void testGenerateQrPayload_Success() {
        // Arrange
        sampleRecord.setStatus("CERTIFIED");
        when(traceRecordRepository.findByProductionBatch_ProductionId(BATCH_ID))
                .thenReturn(Optional.of(sampleRecord));

        // Act
        String payload = traceabilityService.generateQrPayload(BATCH_ID);

        // Assert: Verify the String.format logic
        assertNotNull(payload);
        assertTrue(payload.startsWith("FoodChainX-Trace:1"));
        assertTrue(payload.contains("Product:Organic Basmati"));
        assertTrue(payload.contains("Status:CERTIFIED"));
    }
}