package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.report.ReportResponseDto;
import com.cts.foodchainx.model.Report;
import com.cts.foodchainx.repository.ProductionBatchRepository;
import com.cts.foodchainx.repository.ReportRepository;
import com.cts.foodchainx.repository.ShipmentRepository;
import com.cts.foodchainx.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for generating scoped performance reports without hardcoded values.
 * Logic pulls real-time data from Production, Shipment, and Inventory repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProductionBatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    private static final String PERCENT_FORMAT = "%.2f%%";
    private static final String STATUS_PASSED = "PASSED";
    private static final String STATUS_DELIVERED = "DELIVERED";

    public ReportResponseDto generateScopedPerformance(String scope) {
        log.info("Calculating real-time metrics for scope: {}", scope);
        
        Map<String, Object> metricsMap = new HashMap<>();
        String normalizedScope = scope.toUpperCase();

        switch (normalizedScope) {
            case "FARM":
                calculateFarmMetrics(metricsMap);
                break;

            case "DISTRIBUTOR":
                calculateDistributorMetrics(metricsMap);
                break;

            case "RETAILER":
                calculateRetailerMetrics(metricsMap);
                break;

            default:
                throw new IllegalArgumentException("Invalid scope. Must be FARM, DISTRIBUTOR, or RETAILER.");
        }

        return ReportResponseDto.builder()
                .reportId(System.currentTimeMillis())
                .scope(normalizedScope)
                .metrics(metricsMap)
                .generatedDate(LocalDateTime.now())
                .build();
    }

    private void calculateFarmMetrics(Map<String, Object> metrics) {
        long total = batchRepository.count();
        long passed = batchRepository.findAll().stream()
                .filter(b -> STATUS_PASSED.equalsIgnoreCase(b.getQualityStatus()))
                .count();
        
        double rate = total > 0 ? (double) passed / total * 100 : 0;
        
        metrics.put("harvestQualityPassRate", String.format(PERCENT_FORMAT, rate));
        metrics.put("totalBatchesProduced", total);
    }

    private void calculateDistributorMetrics(Map<String, Object> metrics) {
        long totalShipments = shipmentRepository.count();
        long deliveredOnTime = shipmentRepository.findAll().stream()
                .filter(s -> STATUS_DELIVERED.equalsIgnoreCase(s.getStatus()))
                // In Phase 2, you could add: && s.getActualDate().isBefore(s.getExpectedDate())
                .count();

        double efficiency = totalShipments > 0 ? (double) deliveredOnTime / totalShipments * 100 : 0;

        metrics.put("deliverySuccessRate", String.format(PERCENT_FORMAT, efficiency));
        metrics.put("activeShipmentsCount", totalShipments);
    }

    private void calculateRetailerMetrics(Map<String, Object> metrics) {
        long totalInventoryItems = inventoryRepository.count();
        long verifiedOrigins = inventoryRepository.findAll().stream()
                .filter(i -> i.getBatchId() != null) // Ensuring every item is linked to a batch
                .count();

        double verificationRate = totalInventoryItems > 0 ? (double) verifiedOrigins / totalInventoryItems * 100 : 0;

        metrics.put("totalInventoryUnits", totalInventoryItems);
        metrics.put("traceabilityVerificationRate", String.format(PERCENT_FORMAT, verificationRate));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void archiveDailyReports() {
        for (String s : new String[]{"FARM", "DISTRIBUTOR", "RETAILER"}) {
            saveToDatabase(generateScopedPerformance(s));
        }
    }

    private void saveToDatabase(ReportResponseDto dto) {
        try {
            Report entity = new Report();
            entity.setScope(dto.getScope());
            entity.setGeneratedDate(LocalDate.now());
            entity.setMetrics(objectMapper.writeValueAsString(dto.getMetrics()));
            reportRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to archive {} report", dto.getScope(), e);
        }
    }
}