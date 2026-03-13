package com.cts.foodchainx.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.model.Farm;
import com.cts.foodchainx.model.ProductionBatch;
import com.cts.foodchainx.model.QualityCheck;
import com.cts.foodchainx.model.TraceRecord;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.FarmRepository;
import com.cts.foodchainx.repository.ProductionBatchRepository;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductionBatchServiceTest {

    @Mock private ProductionBatchRepository batchRepository;
    @Mock private FarmRepository farmRepository;
    @Mock private QualityLoggingRepository qualityRepo;
    @Mock private UserRepository userRepository;
    @Mock private TraceRecordRepository traceRecordRepository;

    @InjectMocks private ProductionBatchService batchService;

    private Farm sampleFarm;
    private ProductionBatch sampleBatch;
    private User sampleInspector;
    private final Long BATCH_ID = 100L;
    private final Long FARM_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleFarm = new Farm();
        sampleFarm.setFarmId(FARM_ID);
        sampleFarm.setName("Green Valley");

        sampleBatch = ProductionBatch.builder()
                .productionId(BATCH_ID)
                .farm(sampleFarm)
                .cropType("Corn")
                .quantity(500.0)
                .qualityStatus("PENDING")
                .qualityChecks(new ArrayList<>())
                .build();

        sampleInspector = new User();
        sampleInspector.setUserId(5L);
    }

    // --- 1. CREATE BATCH TEST ---
    @Test
    void testCreateBatch_Success() {
        BatchRequestDto request = new BatchRequestDto(FARM_ID, "Corn", 500.0, LocalDate.now());
        
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));
        when(batchRepository.save(any(ProductionBatch.class))).thenReturn(sampleBatch);

        BatchResponseDto response = batchService.createBatch(request);

        assertNotNull(response);
        assertEquals("PENDING", response.getQualityStatus());
        verify(traceRecordRepository, times(1)).save(any(TraceRecord.class));
    }

    // --- 2. PERFORM QUALITY CHECK TEST ---
    @Test
    void testPerformQualityCheck_Success() {
        QualityRequestDto qDto = new QualityRequestDto(BATCH_ID, 5L, "Good quality", "PASSED");
        TraceRecord record = new TraceRecord();

        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleInspector));
        when(traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(BATCH_ID))
                .thenReturn(List.of(record));

        String result = batchService.performQualityCheck(qDto);

        assertTrue(result.contains("PASSED"));
        assertEquals("PASSED", sampleBatch.getQualityStatus());
        verify(qualityRepo).save(any(QualityCheck.class));
        verify(batchRepository).save(sampleBatch);
    }

    // --- 3. GET BATCH DETAIL TEST ---
    @Test
    void testGetBatchDetail_Success() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));

        BatchDetailResponseDto details = batchService.getBatchDetail(BATCH_ID);

        assertEquals("Corn", details.getCropType());
        assertEquals("Green Valley", details.getFarmName());
    }

    // --- 4. GET BATCHES BY FARM ---
    @Test
    void testGetBatchesByFarm_Success() {
        when(batchRepository.findByFarm_FarmId(FARM_ID)).thenReturn(List.of(sampleBatch));

        List<BatchResponseDto> result = batchService.getBatchesByFarm(FARM_ID);

        assertFalse(result.isEmpty());
        assertEquals(BATCH_ID, result.get(0).getBatchId());
    }

    // --- 5. DELETE BATCH - HAPPY PATH (PENDING) ---
    @Test
    void testDeleteBatch_Success() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));

        String result = batchService.deleteBatch(BATCH_ID);

        assertTrue(result.contains("deleted successfully"));
        verify(batchRepository).delete(sampleBatch);
    }

    // --- 6. DELETE BATCH - FAILURE (ALREADY PASSED) ---
    @Test
    void testDeleteBatch_FailsIfPassed() {
        sampleBatch.setQualityStatus("PASSED");
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));

        assertThrows(RuntimeException.class, () -> batchService.deleteBatch(BATCH_ID));
        verify(batchRepository, never()).delete(any());
    }
}
