package com.cts.FoodChainX.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.aspect.Auditable; // Added Import
import com.cts.FoodChainX.dto.batch.BatchDetailResponseDto;
import com.cts.FoodChainX.dto.batch.BatchRequestDto;
import com.cts.FoodChainX.dto.batch.BatchResponseDto;
import com.cts.FoodChainX.dto.quality.QualityRequestDto;
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.model.ProductionBatch;
import com.cts.FoodChainX.model.QualityCheck;
import com.cts.FoodChainX.model.TraceRecord;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.ProductionBatchRepository;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import com.cts.FoodChainX.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final QualityLoggingRepository qualityRepo;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    // --- PRODUCTION BATCH METHODS ---

    @Transactional
    @Auditable(action = "HARVEST_BATCH", resource = "PRODUCTION_BATCH") // Added Annotation
    public BatchResponseDto createBatch(BatchRequestDto dto) {
        Farm farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        ProductionBatch batch = ProductionBatch.builder()
                .farm(farm)
                .cropType(dto.getCropType())
                .quantity(dto.getQuantity())
                .harvestDate(dto.getHarvestDate()) 
                .qualityStatus("PENDING")          
                .build();

        ProductionBatch saved = batchRepository.save(batch);

        // --- AUTOMATION: Create Initial Trace Record ---
        TraceRecord initialTrace = new TraceRecord();
        initialTrace.setProductionBatch(saved);
        initialTrace.setFarm(farm);
        initialTrace.setStatus("HARVESTED_AT_FARM");
        initialTrace.setDate(LocalDate.now());
        traceRecordRepository.save(initialTrace);

        return new BatchResponseDto(saved.getProductionId(), saved.getQualityStatus());
    }

    @Transactional
    @Auditable(action = "PERFORM_QUALITY_CHECK", resource = "PRODUCTION_BATCH") // Added Annotation
    public String performQualityCheck(QualityRequestDto dto) {
        // 1. Fetch dependencies
        ProductionBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        User inspectorUser = userRepository.findById(dto.getInspectorId())
                .orElseThrow(() -> new RuntimeException("Inspector not found"));

        // 2. Save the Quality Report
        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspectorUser)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        qualityRepo.save(check);

        // 3. Update Batch Status
        batch.setQualityStatus(dto.getStatus());
        batchRepository.save(batch);

        // 4. AUTOMATED SIDE EFFECT: Insert row into Trace Record
        TraceRecord qualityTrace = new TraceRecord();
        qualityTrace.setProductionBatch(batch);
        qualityTrace.setFarm(batch.getFarm());

        String traceStatus = "PASSED".equalsIgnoreCase(dto.getStatus())
                ? "QUALITY_CERTIFIED" : "QUALITY_REJECTED";

        qualityTrace.setStatus(traceStatus);
        qualityTrace.setDate(LocalDate.now());

        traceRecordRepository.save(qualityTrace);

        return "Inspection completed. Trace updated to " + traceStatus;
    }

    // --- ADDITIONAL METHODS ---

    @Transactional(readOnly = true)
    public BatchDetailResponseDto getBatchDetail(Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        List<String> findingsList = batch.getQualityChecks().stream()
                .map(QualityCheck::getFindings)
                .collect(Collectors.toList());

        return new BatchDetailResponseDto(
                batch.getProductionId(),
                batch.getCropType(),
                batch.getQuantity(),
                batch.getQualityStatus(),
                findingsList,
                batch.getFarm().getName(),
                batch.getFarm().getLocation()
        );
    }

    @Transactional(readOnly = true)
    public BatchResponseDto getBatchById(Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        return new BatchResponseDto(
                batch.getProductionId(),
                batch.getQualityStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<BatchResponseDto> getBatchesByFarm(Long farmId) {
        return batchRepository.findByFarm_FarmId(farmId).stream()
                .map(batch -> new BatchResponseDto(
                        batch.getProductionId(),
                        batch.getQualityStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(action = "DELETE_BATCH", resource = "PRODUCTION_BATCH") // Added Annotation
    public String deleteBatch(Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if ("PASSED".equals(batch.getQualityStatus())) {
            throw new RuntimeException("Cannot delete a batch that has already passed quality check.");
        }

        batchRepository.delete(batch);
        return "Batch " + batchId + " deleted successfully.";
    }
}