package com.cts.FoodChainX.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.aspect.Auditable;
import com.cts.FoodChainX.dto.quality.QualityRequestDto;
import com.cts.FoodChainX.dto.quality.QualityResponseDto;
import com.cts.FoodChainX.model.ProductionBatch;
import com.cts.FoodChainX.model.QualityCheck;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.ProductionBatchRepository;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QualityCheckService {

    private final QualityLoggingRepository qualityRepo;
    private final ProductionBatchRepository batchRepo;
    private final UserRepository userRepo;

    // 1. PERFORM INSPECTION & UPDATE PRODUCTION STATUS
    @Transactional
    @Auditable(action = "PERFORM_INSPECTION", resource = "QUALITY_CHECK") // ADD THIS
    public String inspectBatch(QualityRequestDto dto) {
        // Find the Batch
        ProductionBatch batch = batchRepo.findById(dto.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Find the Inspector (Regulator)
        User inspector = userRepo.findById(dto.getInspectorId())
                .orElseThrow(() -> new RuntimeException("Inspector not found"));

        // Create the Quality Log (The "Report")
        QualityCheck check = QualityCheck.builder()
                .batch(batch)
                .inspector(inspector)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        
        qualityRepo.save(check);

        // Reflect the result in the Production Table
        batch.setQualityStatus(dto.getStatus()); 
        batchRepo.save(batch); 

        return "Inspection completed. Batch " + dto.getBatchId() + " updated to: " + dto.getStatus();
    }

    // 2. GET BATCHES BY STATUS (Filtered Views)
    // You can call this for "PENDING", "APPROVED", or "REJECTED"
 public List<QualityResponseDto> getInspectionsByStatus(String status) {
    // 1. The database now returns ONLY the rows that match the status
    return qualityRepo.findByStatusIgnoreCase(status).stream()
            .map(q -> new QualityResponseDto(
                    q.getQualityId(), 
                    q.getDate(), 
                    q.getStatus(),
                    q.getFindings()))
            .collect(Collectors.toList());
}

    // 3. DELETE LOG & RESET BATCH TO PENDING
    @Transactional
    @Auditable(action = "DELETE_QUALITY_LOG", resource = "QUALITY_CHECK") // ADD THIS
    public String removeQualityLog(Long qualityId) {
        QualityCheck check = qualityRepo.findById(qualityId)
                .orElseThrow(() -> new RuntimeException("Log not found"));

        // Reset the Batch back to PENDING so it can be re-inspected
        ProductionBatch batch = check.getBatch();
        batch.setQualityStatus("PENDING");
        batchRepo.save(batch);

        qualityRepo.delete(check);
        return "Log " + qualityId + " deleted. Batch status reset to PENDING.";
    }
}
