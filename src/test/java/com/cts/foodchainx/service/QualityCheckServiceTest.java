package com.cts.foodchainx.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.dto.quality.QualityResponseDto;
import com.cts.foodchainx.enums.QualityStatus; 
import com.cts.foodchainx.model.ProductionBatch;
import com.cts.foodchainx.model.QualityCheck;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.ProductionBatchRepository;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.serviceimpl.QualityCheckServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class QualityCheckServiceTest {

    @Mock private QualityLoggingRepository qualityRepo;
    @Mock private ProductionBatchRepository batchRepo;
    @Mock private UserRepository userRepo;
    @Mock private TraceRecordRepository traceRecordRepository;

    @InjectMocks private QualityCheckServiceImpl qualityCheckService;

    private ProductionBatch sampleBatch;
    private User sampleInspector;
    private QualityCheck sampleCheck;
    private final Long BATCH_ID = 200L;
    private final Long QUALITY_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setQualityStatus(QualityStatus.PENDING); // Updated to Enum

        sampleInspector = new User();
        sampleInspector.setUserId(10L);

        sampleCheck = QualityCheck.builder()
                .qualityId(QUALITY_ID)
                .batch(sampleBatch)
                .inspector(sampleInspector)
                .status(QualityStatus.PASSED) // Updated to Enum
                .findings("Excellent quality")
                .date(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Inspect Batch - Success and Trace Update")
    void testInspectBatch_Success() {
        // Arrange
        QualityRequestDto request = new QualityRequestDto(BATCH_ID, 10L, "Passed test", QualityStatus.PASSED);

        when(batchRepo.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));
        when(userRepo.findById(10L)).thenReturn(Optional.of(sampleInspector));
        when(qualityRepo.save(any(QualityCheck.class))).thenReturn(sampleCheck);

        // Act
        String result = qualityCheckService.inspectBatch(request);

        // Assert
        assertTrue(result.contains("Inspection completed"));
        assertEquals(QualityStatus.PASSED, sampleBatch.getQualityStatus());
        verify(qualityRepo, times(1)).save(any(QualityCheck.class));
        verify(batchRepo, times(1)).save(sampleBatch);
        verify(traceRecordRepository, times(1)).save(any());
    }

@Test
    @DisplayName("Get Inspections By Status - Success")
    void testGetInspectionsByStatus_Success() {
        // Arrange
        when(qualityRepo.findByStatus(QualityStatus.PASSED)).thenReturn(List.of(sampleCheck));

        // Act
        List<QualityResponseDto> results = qualityCheckService.getInspectionsByStatus(QualityStatus.PASSED);

        // Assert
        assertFalse(results.isEmpty());
        // FIXED: Use .name() to ensure we are comparing String to String
        assertEquals(QualityStatus.PASSED, results.get(0).getStatus()); 
        verify(qualityRepo, times(1)).findByStatus(QualityStatus.PASSED);
    }

    @Test
    @DisplayName("Remove Quality Log - Reset to PENDING")
    void testRemoveQualityLog_Success() {
        // Arrange
        sampleBatch.setQualityStatus(QualityStatus.PASSED);
        when(qualityRepo.findById(QUALITY_ID)).thenReturn(Optional.of(sampleCheck));

        // Act
        String result = qualityCheckService.removeQualityLog(QUALITY_ID);

        // Assert
        assertTrue(result.contains("reset to PENDING"));
        assertEquals(QualityStatus.PENDING, sampleBatch.getQualityStatus());
        verify(qualityRepo).delete(sampleCheck);
        verify(batchRepo).save(sampleBatch);
    }

    @Test
    @DisplayName("Inspect Batch - Throws Exception when Batch Not Found")
    void testInspectBatch_NotFound() {
        // Arrange
        QualityRequestDto request = new QualityRequestDto(999L, 10L, "Findings", QualityStatus.PASSED);
        when(batchRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> qualityCheckService.inspectBatch(request));
    }
}