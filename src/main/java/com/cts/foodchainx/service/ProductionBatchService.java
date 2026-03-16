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
import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.enums.TraceStatus;
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

/**
 * Service responsible for managing the lifecycle of Production Batches.
 * <p>This service coordinates between Farms, Quality Checks, and Traceability Records. 
 * It ensures that every harvest is tracked from the moment it is created.</p>
 */
@Service
@RequiredArgsConstructor
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final QualityLoggingRepository qualityRepo;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    /**
     * Creates a new production batch and initializes its traceability history.
     * <p><b>Flow:</b>
     * 1. Validates the Farm existence.
     * 2. Persists the new Batch with "PENDING" status.
     * 3. Creates an initial TraceRecord marked as "HARVESTED_AT_FARM".</p>
     * * @param dto Data transfer object containing batch details.
     * @return BatchResponseDto containing the new ID and status.
     * @throws FarmNotFoundException if the associated Farm ID is invalid.
     */
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
                .qualityStatus(QualityStatus.PENDING)
                .build();

        ProductionBatch saved = Objects.requireNonNull(batchRepository.save(batch));
        
        TraceRecord initialTrace = new TraceRecord();
        initialTrace.setProductionBatch(saved);
        initialTrace.setFarm(farm);
        initialTrace.setStatus(TraceStatus.HARVESTED);
        initialTrace.setDate(LocalDate.now());
        traceRecordRepository.save(initialTrace);

        return new BatchResponseDto(saved.getProductionId(), saved.getQualityStatus().name());
    }

    /**
     * Records a quality inspection and updates the batch status across the system.
     * <p>This method updates both the ProductionBatch entity and adds a new entry 
     * to the Traceability logs based on the inspection result.</p>
     * * @param dto Contains inspection findings, inspector ID, and the result status.
     * @return A status message indicating the completion of the check and trace update.
     * @throws EntityNotFoundException if the Batch or Inspector does not exist.
     */
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
        
        Objects.requireNonNull(qualityRepo.save(check));

        batch.setQualityStatus(dto.getStatus());
        batchRepository.save(batch);

        TraceRecord qualityTrace = new TraceRecord();
        qualityTrace.setProductionBatch(batch);
        qualityTrace.setFarm(batch.getFarm());

        TraceStatus traceStatus = (dto.getStatus() == QualityStatus.PASSED)
                ? TraceStatus.QUALITY_CERTIFIED : TraceStatus.QUALITY_REJECTED;

        qualityTrace.setStatus(traceStatus);
        qualityTrace.setDate(LocalDate.now());
        traceRecordRepository.save(qualityTrace);

        return "Inspection completed. Trace updated to " + traceStatus;
    }

    /**
     * Retrieves full details of a batch, including farm information and a list of all quality findings.
     * * @param batchId The ID of the batch to retrieve.
     * @return BatchDetailResponseDto containing comprehensive batch data.
     * @throws EntityNotFoundException if the batch ID does not exist.
     */
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
                batch.getQualityStatus().name(),
                findingsList,
                batch.getFarm().getName(),
                batch.getFarm().getLocation()
        );
    }

    /**
     * Retrieves basic ID and Status for a specific batch.
     * * @param batchId The ID of the batch.
     * @return BatchResponseDto containing ID and status.
     * @throws BatchNotFoundException if ID is missing in database.
     */
    @Transactional(readOnly = true)
    public BatchResponseDto getBatchById(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        return new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus().name());
    }

    /**
     * Retrieves all batches associated with a specific farm.
     * * @param farmId The ID of the farm.
     * @return A list of BatchResponseDto objects.
     */
    public List<BatchResponseDto> getBatchesByFarm(@NonNull Long farmId) {
        return batchRepository.findByFarm_FarmId(farmId).stream()
                .map(batch -> new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus().name()))
                .toList();
    }

    /**
     * Deletes a production batch from the system.
     * <p><b>Security Rule:</b> A batch cannot be deleted if it has already passed 
     * quality check to prevent tampering with certified supply chain data.</p>
     * * @param batchId The ID of the batch to delete.
     * @return Success message string.
     * @throws BatchNotFoundException if batch is not found.
     * @throws IllegalStateException if the batch status is already 'PASSED'.
     */
    @Transactional
    @Auditable(action = "DELETE_BATCH", resource = "PRODUCTION_BATCH")
    public String deleteBatch(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        if (batch.getQualityStatus() == QualityStatus.PASSED) {
            throw new IllegalStateException("Cannot delete a batch that has already passed quality check.");
        }

        batchRepository.delete(batch);
        return "Batch " + batchId + " deleted successfully.";
    }
}