package com.cts.FoodChainX.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.FoodChainX.dto.quality.QualityRequestDto;
import com.cts.FoodChainX.dto.quality.QualityResponseDto;
import com.cts.FoodChainX.service.QualityCheckService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quality-checks")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    /**
     * PERFORM INSPECTION & UPDATE PRODUCTION STATUS
     * POST http://localhost:8080/api/quality-checks/inspect
     */
    @PostMapping("/inspect")
    public ResponseEntity<String> performInspection(@RequestBody QualityRequestDto dto) {
        String result = qualityCheckService.inspectBatch(dto);
        return ResponseEntity.ok(result);
    }

    /**
     * GET BATCHES BY STATUS (Filtered Views for Regulator)
     * GET http://localhost:8080/api/quality-checks/status/APPROVED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<QualityResponseDto>> getInspectionsByStatus(@PathVariable String status) {
        List<QualityResponseDto> inspections = qualityCheckService.getInspectionsByStatus(status);
        return ResponseEntity.ok(inspections);
    }

    /**
     * DELETE LOG & RESET BATCH TO PENDING
     * DELETE http://localhost:8080/api/quality-checks/{id}
     */
    @DeleteMapping("/{qualityId}")
    public ResponseEntity<String> deleteQualityLog(@PathVariable Long qualityId) {
        String message = qualityCheckService.removeQualityLog(qualityId);
        return ResponseEntity.ok(message);
    }
}
