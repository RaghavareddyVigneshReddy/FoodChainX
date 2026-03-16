package com.cts.foodchainx.service;

import java.util.List;

import org.springframework.lang.NonNull;

import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.dto.quality.QualityResponseDto;
import com.cts.foodchainx.enums.QualityStatus;

/**
 * Service Interface for managing Quality Inspections and Compliance.
 * <p>
 * Defines operations for batch inspections, status filtering, and quality log maintenance.
 * </p>
 */
public interface QualityCheckService {

    /**
     * Performs a batch inspection, updates batch status, and records a trace event.
     *
     * @param dto Data transfer object containing Batch ID, Inspector ID, findings, and result status.
     * @return Success message including the new status of the batch.
     */
    String inspectBatch(@NonNull QualityRequestDto dto);

    /**
     * Retrieves a list of inspections filtered by their status.
     *
     * @param status The quality status to filter by (e.g., PASSED, REJECTED).
     * @return List of {@link QualityResponseDto} containing summary data of the inspections.
     */
    List<QualityResponseDto> getInspectionsByStatus(QualityStatus status);

    /**
     * Removes a specific quality log and reverts the associated batch status to PENDING.
     *
     * @param qualityId The unique identifier of the quality log to be deleted.
     * @return Confirmation message of the deletion and status reset.
     */
    String removeQualityLog(@NonNull Long qualityId);
}