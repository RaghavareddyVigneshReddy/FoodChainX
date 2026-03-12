package com.cts.foodchainx.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.exception.BatchNotFoundException;
import com.cts.foodchainx.exception.FarmNotFoundException;
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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final QualityLoggingRepository qualityRepo;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    @SuppressWarnings("null")
@Transactional
    @Auditable(action = "HARVEST_BATCH", resource = "PRODUCTION_BATCH")
    public BatchResponseDto createBatch(@NonNull BatchRequestDto dto) {
        Farm farm = farmRepository.findById(Objects.requireNonNull(dto.getFarmId()))
                .orElseThrow(() -> new FarmNotFoundException(dto.getFarmId()));

        ProductionBatch batch = ProductionBatch.builder()
                .farm(farm)
                .cropType(dto.getCropType())
                .quantity(dto.getQuantity())
                .harvestDate(dto.getHarvestDate())
                .qualityStatus("PENDING")
                .build();

        // Wrapped save result to satisfy @NonNull requirement
        ProductionBatch saved = Objects.requireNonNull(batchRepository.save(batch));
        
        TraceRecord initialTrace = new TraceRecord();
        initialTrace.setProductionBatch(saved);
        initialTrace.setFarm(farm);
        initialTrace.setStatus("HARVESTED_AT_FARM");
        initialTrace.setDate(LocalDate.now());
        traceRecordRepository.save(initialTrace);

        return new BatchResponseDto(saved.getProductionId(), saved.getQualityStatus());
    }

    @SuppressWarnings("null")
@Transactional
    @Auditable(action = "PERFORM_QUALITY_CHECK", resource = "PRODUCTION_BATCH")
    public String performQualityCheck(@NonNull QualityRequestDto dto) {
        ProductionBatch batch = batchRepository.findById(Objects.requireNonNull(dto.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));

        User inspectorUser = userRepository.findById(Objects.requireNonNull(dto.getInspectorId()))
                .orElseThrow(() -> new EntityNotFoundException("Inspector not found"));

        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspectorUser)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        
        // Fix: Ensure the saved object is treated as non-null
        Objects.requireNonNull(qualityRepo.save(check));

        batch.setQualityStatus(dto.getStatus());
        batchRepository.save(batch);

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

    @Transactional(readOnly = true)
    public BatchDetailResponseDto getBatchDetail(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found with ID: " + batchId));

        List<String> findingsList = batch.getQualityChecks().stream()
                .map(QualityCheck::getFindings)
                .toList(); 

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
    public BatchResponseDto getBatchById(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        return new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus());
    }

    public List<BatchResponseDto> getBatchesByFarm(@NonNull Long farmId) {
        return batchRepository.findByFarm_FarmId(farmId).stream()
                .map(batch -> new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus()))
                .toList();
    }

    @Transactional
    @Auditable(action = "DELETE_BATCH", resource = "PRODUCTION_BATCH")
    public String deleteBatch(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        if ("PASSED".equalsIgnoreCase(batch.getQualityStatus())) {
            throw new IllegalStateException("Cannot delete a batch that has already passed quality check.");
        }

        batchRepository.delete(batch);
        return "Batch " + batchId + " deleted successfully.";
    }
}