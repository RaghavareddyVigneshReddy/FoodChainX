package com.cts.foodchainx.service;

import java.util.List;

import org.springframework.lang.NonNull;

import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.dto.quality.QualityRequestDto;

/**
 * Service interface for managing the lifecycle of Production Batches.
 * <p>
 * Handles operations from initial harvest creation to quality inspections
 * and traceability logging within the food supply chain.
 * </p>
 */
public interface ProductionBatchService {

    /**
     * Creates a new production batch for a specific farm.
     *
     * @param dto The {@link BatchRequestDto} containing harvest details.
     * @return A {@link BatchResponseDto} with the assigned ID and initial status.
     */
    BatchResponseDto createBatch(@NonNull BatchRequestDto dto);

    /**
     * Performs a quality inspection on a batch and updates its traceability record.
     *
     * @param dto The {@link QualityRequestDto} containing findings and inspector data.
     * @return A message string confirming the inspection result and trace update.
     */
    String performQualityCheck(@NonNull QualityRequestDto dto);

    /**
     * Retrieves comprehensive batch details, including farm info and all quality logs.
     *
     * @param batchId The unique ID of the production batch.
     * @return A {@link BatchDetailResponseDto} containing full batch data.
     */
    BatchDetailResponseDto getBatchDetail(@NonNull Long batchId);

    /**
     * Retrieves basic ID and status for a specific batch.
     *
     * @param batchId The unique ID of the production batch.
     * @return A {@link BatchResponseDto} containing ID and status.
     */
    BatchResponseDto getBatchById(@NonNull Long batchId);

    /**
     * Retrieves all batches associated with a specific farm.
     *
     * @param farmId The unique ID of the farm.
     * @return A list of {@link BatchResponseDto} objects.
     */
    List<BatchResponseDto> getBatchesByFarm(@NonNull Long farmId);

    /**
     * Removes a production batch from the system. 
     * Implementations should prevent deletion of batches that are already certified.
     *
     * @param batchId The unique ID of the batch to delete.
     * @return A confirmation message string.
     */
    String deleteBatch(@NonNull Long batchId);
}