package com.cts.foodchainx.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.dto.quality.QualityResponseDto;
import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.enums.TraceStatus;
import com.cts.foodchainx.model.ProductionBatch;
import com.cts.foodchainx.model.QualityCheck;
import com.cts.foodchainx.model.TraceRecord;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.ProductionBatchRepository;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.QualityCheckService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Concrete implementation of the {@link IQualityCheckService}.
 * <p>
 * Coordinates quality checks with batch management and traceability logging.
 * Uses {@code @Transactional} to ensure atomicity during status transitions.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QualityCheckServiceImpl implements QualityCheckService {

    private final QualityLoggingRepository qualityRepo;
    private final ProductionBatchRepository batchRepo;
    private final UserRepository userRepo;
    private final TraceRecordRepository traceRecordRepository;

    /**
     * {@inheritDoc}
     * <p>This operation updates three different entities (QualityCheck, ProductionBatch, TraceRecord)
     * as a single atomic unit.</p>
     */
    @SuppressWarnings("null")
@Override
    @Transactional
    @Auditable(action = "PERFORM_INSPECTION", resource = "QUALITY_CHECK")
    public String inspectBatch(@NonNull QualityRequestDto dto) {
        log.info("Starting inspection for Batch ID: {}", dto.getBatchId());

        ProductionBatch batch = batchRepo.findById(Objects.requireNonNull(dto.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found with ID: " + dto.getBatchId()));

        User inspector = userRepo.findById(Objects.requireNonNull(dto.getInspectorId()))
                .orElseThrow(() -> new EntityNotFoundException("Inspector not found with ID: " + dto.getInspectorId()));

        // 1. Create and persist Quality Log
        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspector)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        qualityRepo.save(check);

        // 2. Update the Batch Status globally
        batch.setQualityStatus(dto.getStatus()); 
        batchRepo.save(batch); 

        // 3. Record the inspection event in Traceability history
        TraceRecord qualityTrace = new TraceRecord();
        qualityTrace.setProductionBatch(batch);
        qualityTrace.setFarm(batch.getFarm());
        
        TraceStatus traceStatus = (dto.getStatus() == QualityStatus.PASSED) 
                                  ? TraceStatus.QUALITY_CERTIFIED 
                                  : TraceStatus.QUALITY_REJECTED;
                                  
        qualityTrace.setStatus(traceStatus);
        qualityTrace.setDate(LocalDate.now());
        traceRecordRepository.save(qualityTrace);

        return "Inspection completed. Batch " + dto.getBatchId() + " updated to: " + dto.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<QualityResponseDto> getInspectionsByStatus(QualityStatus status) {
        log.debug("Filtering inspections by status: {}", status);
        return qualityRepo.findByStatus(status).stream()
                .map(q -> new QualityResponseDto(
                        q.getQualityId(), 
                        q.getDate(), 
                        q.getStatus(),
                        q.getFindings()))
                .toList(); 
    }

    /**
     * {@inheritDoc}
     * <p>Deleting a quality log triggers an automatic status rollback to 'PENDING'
     * for the associated batch to maintain data integrity.</p>
     */
    @Override
    @Transactional
    @Auditable(action = "DELETE_QUALITY_LOG", resource = "QUALITY_CHECK")
    public String removeQualityLog(@NonNull Long qualityId) {
        log.warn("Removing Quality Log ID: {}. Resetting batch to PENDING.", qualityId);
        
        QualityCheck check = qualityRepo.findById(qualityId)
                .orElseThrow(() -> new EntityNotFoundException("Log not found with ID: " + qualityId));

        // Revert Batch Status to PENDING
        ProductionBatch batch = check.getBatch();
        batch.setQualityStatus(QualityStatus.PENDING);
        batchRepo.save(batch);

        // Remove the log entry
        qualityRepo.delete(check);
        return "Log " + qualityId + " deleted. Batch status reset to PENDING.";
    }
}
