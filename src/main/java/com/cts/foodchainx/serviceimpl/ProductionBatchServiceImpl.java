package com.cts.foodchainx.serviceimpl;

import java.time.LocalDate;
import java.util.List;

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
import com.cts.foodchainx.service.ProductionBatchService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Concrete implementation of the {@link IProductionBatchService}.
 * <p>
 * Coordinates database interactions across multiple repositories to ensure
 * data consistency between batches, quality checks, and traceability logs.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductionBatchServiceImpl implements ProductionBatchService {

    private final ProductionBatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final QualityLoggingRepository qualityRepo;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    /**
     * {@inheritDoc}
     * <p>Creates a batch and an initial 'HARVESTED' trace record in a single transaction.</p>
     */
    @SuppressWarnings("null")
@Override
    @Transactional
    @Auditable(action = "HARVEST_BATCH", resource = "PRODUCTION_BATCH")
    public BatchResponseDto createBatch(@NonNull BatchRequestDto dto) {
        log.info("Processing harvest for Farm ID: {}", dto.getFarmId());
        
        Farm farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new FarmNotFoundException(dto.getFarmId()));

        ProductionBatch batch = ProductionBatch.builder()
                .farm(farm)
                .cropType(dto.getCropType())
                .quantity(dto.getQuantity())
                .harvestDate(dto.getHarvestDate())
                .qualityStatus(QualityStatus.PENDING)
                .build();

        ProductionBatch saved = batchRepository.save(batch);
        
        // Initialize traceability
        TraceRecord initialTrace = new TraceRecord();
        initialTrace.setProductionBatch(saved);
        initialTrace.setFarm(farm);
        initialTrace.setStatus(TraceStatus.HARVESTED);
        initialTrace.setDate(LocalDate.now());
        traceRecordRepository.save(initialTrace);

        return new BatchResponseDto(saved.getProductionId(), saved.getQualityStatus().name());
    }

    /**
     * {@inheritDoc}
     * <p>Transitions the batch status and logs a corresponding certification trace.</p>
     */
    @SuppressWarnings("null")
@Override
    @Transactional
    @Auditable(action = "PERFORM_QUALITY_CHECK", resource = "PRODUCTION_BATCH")
    public String performQualityCheck(@NonNull QualityRequestDto dto) {
        ProductionBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));

        User inspectorUser = userRepository.findById(dto.getInspectorId())
                .orElseThrow(() -> new EntityNotFoundException("Inspector not found"));

        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspectorUser)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        
        qualityRepo.save(check);

        // Update batch status
        batch.setQualityStatus(dto.getStatus());
        batchRepository.save(batch);

        // Update traceability
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BatchResponseDto getBatchById(@NonNull Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        return new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus().name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BatchResponseDto> getBatchesByFarm(@NonNull Long farmId) {
        return batchRepository.findByFarm_FarmId(farmId).stream()
                .map(batch -> new BatchResponseDto(batch.getProductionId(), batch.getQualityStatus().name()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>Enforces a business rule that prevents deletion once a batch is certified PASSED.</p>
     */
    @Override
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