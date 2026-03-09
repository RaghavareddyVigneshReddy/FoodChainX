package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.report.ReportResponseDto;
import com.cts.FoodChainX.repository.ProductionBatchRepository;
import com.cts.FoodChainX.repository.QualityLoggingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProductionBatchRepository batchRepository;
    private final QualityLoggingRepository qualityLoggingRepository;

    public ReportResponseDto generateSupplyChainPerformance() {
        log.info("Generating system-wide performance report");
        
        long totalBatches = batchRepository.count();
        long compliantBatches = batchRepository.findAll().stream()
                .filter(b -> "Compliant".equalsIgnoreCase(b.getQualityStatus()))
                .count();

        double complianceRate = totalBatches > 0 ? (double) compliantBatches / totalBatches * 100 : 0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalProductionBatches", totalBatches);
        metrics.put("globalComplianceRate", String.format("%.2f%%", complianceRate));
        metrics.put("sustainabilityScore", "A+"); // Mock metric for Phase 1

        return ReportResponseDto.builder()
                .reportId(System.currentTimeMillis()) // Mock ID for demonstration
                .scope("GLOBAL")
                .metrics(metrics)
                .generatedDate(LocalDateTime.now())
                .build();
    }
}