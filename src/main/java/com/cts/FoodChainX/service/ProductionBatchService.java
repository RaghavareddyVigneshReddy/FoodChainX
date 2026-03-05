package com.cts.FoodChainX.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.dto.batch.BatchDetailResponseDto;
import com.cts.FoodChainX.dto.batch.BatchRequestDto;
import com.cts.FoodChainX.dto.batch.BatchResponseDto;
import com.cts.FoodChainX.dto.quality.QualityRequestDto; // Ensure this package exists
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.model.ProductionBatch;
import com.cts.FoodChainX.model.QualityCheck;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.ProductionBatchRepository;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ProductionBatchService{

    private final ProductionBatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final QualityLoggingRepository qualityRepo;
    private final UserRepository userRepository;

    // --- PRODUCTION BATCH METHODS ---

    public BatchResponseDto createBatch(BatchRequestDto dto) {
        Farm farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        ProductionBatch batch = ProductionBatch.builder()
                .farm(farm)
                .cropType(dto.getCropType())
                .quantity(dto.getQuantity())
                .harvestDate(dto.getHarvestDate()) // Farmer provides this
                .qualityStatus("PENDING")          // Initial Status
                .build();

        ProductionBatch saved = batchRepository.save(batch);
        return new BatchResponseDto(saved.getProductionId(), saved.getQualityStatus());
    }

    // --- QUALITY CHECK METHODS ---

  @Transactional
    public String performQualityCheck(QualityRequestDto dto) {
        ProductionBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
                // 2. Find the User (Inspector) -> THIS IS THE MISSING STEP
    var inspectorUser = userRepository.findById(dto.getInspectorId())
            .orElseThrow(() -> new RuntimeException("Inspector/User not found"));

        // Fix: Use scalar IDs to match your QualityCheck model
        QualityCheck check = QualityCheck.builder()
                .batch(batch) // scalar ID instead of setBatch(batch)
                .inspector(inspectorUser)
                .findings(dto.getFindings())
                .status(dto.getStatus())
                .date(LocalDate.now())
                .build();
        qualityRepo.save(check);

        // Update the batch status
        batch.setQualityStatus(dto.getStatus());
        batchRepository.save(batch); 

        return "Batch status updated to: " + dto.getStatus();
    }
    // --- ADDITIONAL METHODS ---


    @Transactional(readOnly = true)
public BatchDetailResponseDto getBatchDetail(Long batchId) {
    // 1. Fetch the batch or throw an error if the ID is wrong
    ProductionBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

    // 2. Map the findings from the related QualityCheck list
    // This works because of the @OneToMany relationship in your Model
    List<String> findingsList = batch.getQualityChecks().stream()
            .map(QualityCheck::getFindings)
            .collect(Collectors.toList());

    // 3. Create the DTO using data from 3 different places:
    // - The Batch (Crop/Quantity)
    // - The Farm (Name/Location)
    // - The QualityChecks (Findings)
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
    // 1. Get a single batch by ID (Useful for the Farmer or Regulator)
    public BatchResponseDto getBatchById(Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        return new BatchResponseDto(
                batch.getProductionId(),
                batch.getQualityStatus()
        );
    }

    // 2. Get all batches for a specific Farm (Essential for the Farmer's dashboard)
    public List<BatchResponseDto> getBatchesByFarm(Long farmId) {
        // Assuming your repository has: findByFarm_FarmId(Long farmId)
        return batchRepository.findByFarm_FarmId(farmId).stream()
                .map(batch -> new BatchResponseDto(
                        batch.getProductionId(),
                        batch.getQualityStatus()))
                .collect(Collectors.toList());
    }

    // 3. Delete a Batch
    @Transactional
    public String deleteBatch(Long batchId) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Logic check: Usually, you shouldn't delete a batch if it's already "PASSED" 
        // because it might be linked to a shipment already.
        if ("PASSED".equals(batch.getQualityStatus())) {
            throw new RuntimeException("Cannot delete a batch that has already passed quality check.");
        }

        // Delete associated Quality Checks first if your @OneToMany doesn't have CascadeType.REMOVE
        batchRepository.delete(batch);
        return "Batch " + batchId + " deleted successfully.";
    }
}
