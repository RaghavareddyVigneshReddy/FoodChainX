package com.cts.foodchainx.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.enums.QualityStatus; // Updated import
import com.cts.foodchainx.enums.TraceStatus;   // Updated import
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
import com.cts.foodchainx.serviceimpl.ProductionBatchServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductionBatchServiceTest {

    @Mock private ProductionBatchRepository batchRepository;
    @Mock private FarmRepository farmRepository;
    @Mock private QualityLoggingRepository qualityRepo; // Ensure this matches your Repository name
    @Mock private UserRepository userRepository;
    @Mock private TraceRecordRepository traceRecordRepository;

    @InjectMocks private ProductionBatchServiceImpl batchService;

    private Farm sampleFarm;
    private ProductionBatch sampleBatch;
    private User sampleInspector;
    private final Long batchId = 100L;
    private final Long farmId = 1L;

    @BeforeEach
    void setUp() {
        sampleFarm = new Farm();
        sampleFarm.setFarmId(farmId);
        sampleFarm.setName("Green Valley");

        sampleBatch = ProductionBatch.builder()
                .productionId(batchId)
                .farm(sampleFarm)
                .cropType("Corn")
                .quantity(500.0)
                .qualityStatus(QualityStatus.PENDING) // Updated to Enum
                .qualityChecks(new ArrayList<>())
                .build();

        sampleInspector = new User();
        sampleInspector.setUserId(5L);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create Batch - Success and Initial Trace Harvested")
    void testCreateBatch_Success() {
        BatchRequestDto request = new BatchRequestDto(farmId, "Corn", 500.0, LocalDate.now());
        
        when(farmRepository.findById(farmId)).thenReturn(Optional.of(sampleFarm));
        when(batchRepository.save(any(ProductionBatch.class))).thenReturn(sampleBatch);

        BatchResponseDto response = batchService.createBatch(request);

        assertNotNull(response);
        // We use .name() if the DTO returns a String status
        assertEquals(QualityStatus.PENDING.name(), response.getQualityStatus());
        verify(traceRecordRepository, times(1)).save(any(TraceRecord.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Perform Quality Check - Success (PASSED)")
    void testPerformQualityCheck_Success() {
        // Arrange: Updated DTO to use QualityStatus Enum
        QualityRequestDto qDto = new QualityRequestDto(batchId, 5L, "Good quality", QualityStatus.PASSED);
        
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(sampleBatch));
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleInspector));
        when(qualityRepo.save(any(QualityCheck.class))).thenReturn(new QualityCheck());
        when(batchRepository.save(any(ProductionBatch.class))).thenReturn(sampleBatch);

        // Act
        String result = batchService.performQualityCheck(qDto);

        // Assert: Using TraceStatus Enum names in result string check
        assertTrue(result.contains(TraceStatus.QUALITY_CERTIFIED.name()));
        assertEquals(QualityStatus.PASSED, sampleBatch.getQualityStatus());
        
        verify(qualityRepo).save(any(QualityCheck.class));
        verify(batchRepository).save(sampleBatch);
        verify(traceRecordRepository).save(any(TraceRecord.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Get Batch Detail - Success")
    void testGetBatchDetail_Success() {
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(sampleBatch));

        BatchDetailResponseDto details = batchService.getBatchDetail(batchId);

        assertEquals("Corn", details.getCropType());
        assertEquals("Green Valley", details.getFarmName());
        assertEquals(QualityStatus.PENDING.name(), details.getQualityStatus());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete Batch - Success (PENDING Status)")
    void testDeleteBatch_Success() {
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(sampleBatch));

        String result = batchService.deleteBatch(batchId);

        assertTrue(result.contains("deleted successfully"));
        verify(batchRepository).delete(sampleBatch);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete Batch - Fails (Status is already PASSED)")
    void testDeleteBatch_FailsIfPassed() {
        // Arrange
        sampleBatch.setQualityStatus(QualityStatus.PASSED); // Updated to Enum
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(sampleBatch));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> batchService.deleteBatch(batchId));
        verify(batchRepository, never()).delete(any());
    }
}