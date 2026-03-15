package com.cts.foodchainx.service;

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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing Quality Inspections and Compliance.
 * <p>This service handles the creation of quality logs, status updates for batches, 
 * and ensures traceability records are generated for every inspection event.</p>
 */
@Service
@RequiredArgsConstructor
public class QualityCheckService {

    private final QualityLoggingRepository qualityRepo;
    private final ProductionBatchRepository batchRepo;
    private final UserRepository userRepo;
    private final TraceRecordRepository traceRecordRepository;

    /**
     * Performs a batch inspection, updates batch status, and records a trace event.
     * <p><b>Atomic Operation:</b> This method is transactional. If the status update or trace 
     * recording fails, the quality log will not be saved.</p>
     * * @param dto Data transfer object containing Batch ID, Inspector ID, findings, and result status.
     * @return Success message including the new status of the batch.
     * @throws EntityNotFoundException if the Batch or Inspector ID is invalid.
     */
    @SuppressWarnings("null")
    @Transactional
    @Auditable(action = "PERFORM_INSPECTION", resource = "QUALITY_CHECK")
    public String inspectBatch(@NonNull QualityRequestDto dto) {
        // Validation and Fetching
        ProductionBatch batch = batchRepo.findById(Objects.requireNonNull(dto.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found with ID: " + dto.getBatchId()));

        User inspector = userRepo.findById(Objects.requireNonNull(dto.getInspectorId()))
                .orElseThrow(() -> new EntityNotFoundException("Inspector not found with ID: " + dto.getInspectorId()));

        // 1. Create Quality Log
        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspector)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        
        qualityRepo.save(check);

        // 2. Update Batch Status
        batch.setQualityStatus(dto.getStatus()); 
        batchRepo.save(batch); 

        // 3. Update Traceability Records
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
     * Retrieves a list of inspections filtered by their status (e.g., PASSED, REJECTED).
     * * @param status The status string to filter by (case-insensitive).
     * @return List of {@link QualityResponseDto} containing summary data of the inspections.
     */
    public List<QualityResponseDto> getInspectionsByStatus(QualityStatus status) {
        return qualityRepo.findByStatus(status).stream()
                .map(q -> new QualityResponseDto(
                        q.getQualityId(), 
                        q.getDate(), 
                        q.getStatus(),
                        q.getFindings()))
                .toList(); 
    }

    /**
     * Removes a specific quality log and reverts the associated batch status.
     * <p><b>Logic:</b> When a log is deleted, the system assumes the batch must return 
     * to a "PENDING" state until a new inspection is performed.</p>
     * * @param qualityId The ID of the quality log to be deleted.
     * @return Confirmation message of the deletion and status reset.
     * @throws EntityNotFoundException if the log ID does not exist.
     */
    @Transactional
    @Auditable(action = "DELETE_QUALITY_LOG", resource = "QUALITY_CHECK")
    public String removeQualityLog(@NonNull Long qualityId) {
        QualityCheck check = qualityRepo.findById(qualityId)
                .orElseThrow(() -> new EntityNotFoundException("Log not found with ID: " + qualityId));

        // Revert Batch Status to PENDING
        ProductionBatch batch = check.getBatch();
        batch.setQualityStatus(QualityStatus.PENDING);
        batchRepo.save(batch);

        // Delete the log
        qualityRepo.delete(Objects.requireNonNull(check));
        return "Log " + qualityId + " deleted. Batch status reset to PENDING.";
    }
}