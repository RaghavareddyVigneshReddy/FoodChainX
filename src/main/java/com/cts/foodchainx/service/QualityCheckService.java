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

@Service
@RequiredArgsConstructor
public class QualityCheckService {

    private final QualityLoggingRepository qualityRepo;
    private final ProductionBatchRepository batchRepo;
    private final UserRepository userRepo;
    private final TraceRecordRepository traceRecordRepository;

    // 1. PERFORM INSPECTION & UPDATE PRODUCTION STATUS
    @SuppressWarnings("null")
@Transactional
    @Auditable(action = "PERFORM_INSPECTION", resource = "QUALITY_CHECK")
    public String inspectBatch(@NonNull QualityRequestDto dto) {
        // Fix: Objects.requireNonNull ensures we don't pass null IDs to the repository
        ProductionBatch batch = batchRepo.findById(Objects.requireNonNull(dto.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found with ID: " + dto.getBatchId()));

        User inspector = userRepo.findById(Objects.requireNonNull(dto.getInspectorId()))
                .orElseThrow(() -> new EntityNotFoundException("Inspector not found with ID: " + dto.getInspectorId()));

        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspector)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        
        qualityRepo.save(check);

        batch.setQualityStatus(dto.getStatus()); 
        batchRepo.save(batch); 

        TraceRecord qualityTrace = new TraceRecord();
        qualityTrace.setProductionBatch(batch);
        qualityTrace.setFarm(batch.getFarm());
        qualityTrace.setStatus("PASSED".equalsIgnoreCase(dto.getStatus()) ? "QUALITY_CERTIFIED" : "QUALITY_REJECTED");
        qualityTrace.setDate(LocalDate.now());
        traceRecordRepository.save(qualityTrace);

        return "Inspection completed. Batch " + dto.getBatchId() + " updated to: " + dto.getStatus();
    }

    // 2. GET BATCHES BY STATUS (Filtered Views)
    public List<QualityResponseDto> getInspectionsByStatus(String status) {
        return qualityRepo.findByStatusIgnoreCase(status).stream()
                .map(q -> new QualityResponseDto(
                        q.getQualityId(), 
                        q.getDate(), 
                        q.getStatus(),
                        q.getFindings()))
                .toList(); // Fix S6204: Switched to modern .toList()
    }

    // 3. DELETE LOG & RESET BATCH TO PENDING
    @Transactional
    @Auditable(action = "DELETE_QUALITY_LOG", resource = "QUALITY_CHECK")
    public String removeQualityLog(@NonNull Long qualityId) {
        QualityCheck check = qualityRepo.findById(qualityId)
                .orElseThrow(() -> new EntityNotFoundException("Log not found with ID: " + qualityId));

        ProductionBatch batch = check.getBatch();
        batch.setQualityStatus("PENDING");
        batchRepo.save(batch);

        // Fix: Use Objects.requireNonNull to satisfy @NonNull check in repository.delete()
        qualityRepo.delete(Objects.requireNonNull(check));
        return "Log " + qualityId + " deleted. Batch status reset to PENDING.";
    }
}