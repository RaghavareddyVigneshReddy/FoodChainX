package com.cts.foodchainx.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.dto.quality.QualityResponseDto;
import com.cts.foodchainx.service.ProductionBatchService;
import com.cts.foodchainx.service.QualityCheckService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing Quality Assurance operations.
 * Handles crop inspections, status-based filtering for regulators, and quality log management.
 */
@RestController
@RequestMapping("/api/quality-checks")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    private final ProductionBatchService productionBatchService;

    /**
     * Performs a quality inspection on a production batch and updates its global status.
     * This endpoint triggers the transition of a batch from PENDING to APPROVED or REJECTED.
     * <p><b>Endpoint:</b> POST /api/quality-checks/inspect</p>
     * * @param dto The inspection details including batch ID, findings, and result status.
     * @return ResponseEntity containing a success message and HTTP status 200 OK.
     */
    @PostMapping("/inspect")
    public ResponseEntity<String> performInspection(@RequestBody @NonNull QualityRequestDto dto) {
        String result = productionBatchService.performQualityCheck(dto);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves a list of quality inspections filtered by a specific status.
     * Useful for regulators to view only APPROVED or REJECTED batches.
     * <p><b>Endpoint:</b> GET /api/quality-checks/status/{status}</p>
     * * @param status The quality status to filter by (e.g., "APPROVED", "REJECTED").
     * @return ResponseEntity containing a list of matching QualityResponseDto objects.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<QualityResponseDto>> getInspectionsByStatus(@PathVariable String status) {
        List<QualityResponseDto> inspections = qualityCheckService.getInspectionsByStatus(status);
        return ResponseEntity.ok(inspections);
    }

    /**
     * Deletes a specific quality log entry and resets the associated batch back to PENDING status.
     * This is typically used for correcting data entry errors.
     * <p><b>Endpoint:</b> DELETE /api/quality-checks/{qualityId}</p>
     * * @param qualityId The unique identifier of the quality check record to remove.
     * @return ResponseEntity containing a confirmation message and HTTP status 200 OK.
     */
    @DeleteMapping("/{qualityId}")
    public ResponseEntity<String> deleteQualityLog(@PathVariable @NonNull Long qualityId) {
        String message = qualityCheckService.removeQualityLog(qualityId);
        return ResponseEntity.ok(message);
    }
}
