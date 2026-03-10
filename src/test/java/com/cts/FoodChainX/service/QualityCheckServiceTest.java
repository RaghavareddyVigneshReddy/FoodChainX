package com.cts.FoodChainX.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.FoodChainX.dto.quality.QualityRequestDto;
import com.cts.FoodChainX.dto.quality.QualityResponseDto;
import com.cts.FoodChainX.model.ProductionBatch;
import com.cts.FoodChainX.model.QualityCheck;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.ProductionBatchRepository;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class QualityCheckServiceTest {

    @Mock private QualityLoggingRepository qualityRepo;
    @Mock private ProductionBatchRepository batchRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private QualityCheckService qualityCheckService;

    private ProductionBatch sampleBatch;
    private User sampleInspector;
    private QualityCheck sampleCheck;
    private final Long BATCH_ID = 200L;
    private final Long QUALITY_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(BATCH_ID);
        sampleBatch.setQualityStatus("PENDING");

        sampleInspector = new User();
        sampleInspector.setUserId(10L);

        sampleCheck = QualityCheck.builder()
                .qualityId(QUALITY_ID)
                .batch(sampleBatch)
                .inspector(sampleInspector)
                .status("APPROVED")
                .findings("Excellent quality")
                .date(LocalDate.now())
                .build();
    }

    // --- 1. INSPECT BATCH TEST ---
    @Test
    void testInspectBatch_Success() {
        QualityRequestDto request = new QualityRequestDto(BATCH_ID, 10L, "Passed test", "APPROVED");

        when(batchRepo.findById(BATCH_ID)).thenReturn(Optional.of(sampleBatch));
        when(userRepo.findById(10L)).thenReturn(Optional.of(sampleInspector));
        when(qualityRepo.save(any(QualityCheck.class))).thenReturn(sampleCheck);

        String result = qualityCheckService.inspectBatch(request);

        assertTrue(result.contains("Inspection completed"));
        assertEquals("APPROVED", sampleBatch.getQualityStatus());
        verify(qualityRepo, times(1)).save(any(QualityCheck.class));
        verify(batchRepo, times(1)).save(sampleBatch);
    }

    // --- 2. GET INSPECTIONS BY STATUS TEST ---
    @Test
    void testGetInspectionsByStatus_Success() {
        when(qualityRepo.findByStatusIgnoreCase("APPROVED")).thenReturn(List.of(sampleCheck));

        List<QualityResponseDto> results = qualityCheckService.getInspectionsByStatus("APPROVED");

        assertFalse(results.isEmpty());
        assertEquals("APPROVED", results.get(0).getStatus());
        verify(qualityRepo, times(1)).findByStatusIgnoreCase("APPROVED");
    }

    // --- 3. REMOVE QUALITY LOG TEST ---
    @Test
    void testRemoveQualityLog_Success() {
        // Set initial status to APPROVED
        sampleBatch.setQualityStatus("APPROVED");
        
        when(qualityRepo.findById(QUALITY_ID)).thenReturn(Optional.of(sampleCheck));

        String result = qualityCheckService.removeQualityLog(QUALITY_ID);

        assertTrue(result.contains("reset to PENDING"));
        assertEquals("PENDING", sampleBatch.getQualityStatus()); // Verify reset logic
        verify(qualityRepo).delete(sampleCheck);
        verify(batchRepo).save(sampleBatch);
    }

    // --- 4. EXCEPTION TEST (BATCH NOT FOUND) ---
    @Test
    void testInspectBatch_NotFound() {
        QualityRequestDto request = new QualityRequestDto(999L, 10L, "Findings", "APPROVED");
        
        when(batchRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> qualityCheckService.inspectBatch(request));
    }
}