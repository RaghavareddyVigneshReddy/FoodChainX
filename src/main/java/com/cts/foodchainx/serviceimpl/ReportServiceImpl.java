package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.dto.report.ReportResponseDto;
import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.ShipmentStatus;
import com.cts.foodchainx.model.Report;
import com.cts.foodchainx.repository.ProductionBatchRepository;
import com.cts.foodchainx.repository.ReportRepository;
import com.cts.foodchainx.repository.ShipmentRepository;
import com.cts.foodchainx.service.ReportService;
import com.cts.foodchainx.repository.ComplianceRecordRepository;
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
 * Service implementation for generating and archiving performance analytics.
 * <p>
 * This class aggregates real-time data from various repositories (Production, 
 * Logistics, Retail, and Compliance) to calculate Key Performance Indicators (KPIs) 
 * scoped to specific supply chain roles.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ProductionBatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final ReportRepository reportRepository;
    private final ComplianceRecordRepository complianceRepository;
    private final ObjectMapper objectMapper;

    /** Format string for percentage values with two decimal places. */
    private static final String PERCENT_FORMAT = "%.2f%%";

    /**
     * Generates a performance report based on the provided scope.
     * <p>
     * Logic flow:
     * 1. Validates the scope against defined {@link Role} constants.
     * 2. Calculates specific metrics based on the role (Farmer, Distributor, or Retailer).
     * 3. Appends global compliance metrics.
     * </p>
     * * @param scope The supply chain role to filter metrics for (FARMER, DISTRIBUTOR, RETAILER).
     * @return {@link ReportResponseDto} containing a map of calculated metrics and metadata.
     * @throws IllegalArgumentException if the provided scope does not match a valid role.
     */
    @Override
    public ReportResponseDto generateScopedPerformance(String scope) {
        log.info("Calculating real-time metrics for scope: {}", scope);
        
        Map<String, Object> metricsMap = new HashMap<>();
        String normalizedScope = scope.toUpperCase();

        try {
            Role roleScope = Role.valueOf(normalizedScope);
            switch (roleScope) {
                case FARMER:
                    calculateFarmMetrics(metricsMap);
                    break;
                case DISTRIBUTOR:
                    calculateDistributorMetrics(metricsMap);
                    break;
                case RETAILER:
                    calculateRetailerMetrics(metricsMap);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid scope. Must be FARMER, DISTRIBUTOR, or RETAILER.");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid scope: " + scope, e);
        }

        calculateComplianceMetrics(metricsMap);

        return ReportResponseDto.builder()
                .reportId(System.currentTimeMillis())
                .scope(normalizedScope)
                .metrics(metricsMap)
                .generatedDate(LocalDateTime.now())
                .build();
    }

    /**
     * Calculates metrics related to farm production, specifically quality pass rates.
     * * @param metrics Map to populate with farm-related KPIs.
     */
    private void calculateFarmMetrics(Map<String, Object> metrics) {
        long total = batchRepository.count();
        long passed = batchRepository.findAll().stream()
                .filter(b -> b.getQualityStatus() == QualityStatus.PASSED)
                .count();
        
        double rate = total > 0 ? (double) passed / total * 100 : 0;
        
        metrics.put("harvestQualityPassRate", String.format(PERCENT_FORMAT, rate));
        metrics.put("totalBatchesProduced", total);
    }

    /**
     * Calculates logistics efficiency metrics for distributors.
     * * @param metrics Map to populate with delivery-related KPIs.
     */
    private void calculateDistributorMetrics(Map<String, Object> metrics) {
        long totalShipments = shipmentRepository.count();
        long deliveredOnTime = shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        double efficiency = totalShipments > 0 ? (double) deliveredOnTime / totalShipments * 100 : 0;

        metrics.put("deliverySuccessRate", String.format(PERCENT_FORMAT, efficiency));
        metrics.put("activeShipmentsCount", totalShipments);
    }

    /**
     * Calculates retail inventory and traceability verification metrics.
     * * @param metrics Map to populate with inventory KPIs.
     */
    private void calculateRetailerMetrics(Map<String, Object> metrics) {
        long totalInventoryItems = inventoryRepository.count();
        long verifiedOrigins = inventoryRepository.findAll().stream()
                .filter(i -> i.getBatchId() != null)
                .count();

        double verificationRate = totalInventoryItems > 0 ? (double) verifiedOrigins / totalInventoryItems * 100 : 0;

        metrics.put("totalInventoryUnits", totalInventoryItems);
        metrics.put("traceabilityVerificationRate", String.format(PERCENT_FORMAT, verificationRate));
    }

    /**
     * Background task to archive daily reports for all key supply chain roles.
     * <p>
     * Runs daily at midnight (00:00:00).
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void archiveDailyReports() {
        for (String s : new String[]{"FARMER", "DISTRIBUTOR", "RETAILER"}) {
            saveToDatabase(generateScopedPerformance(s));
        }
    }

    /**
     * Persists a generated report to the database for historical tracking.
     * * @param dto The report data to be saved.
     */
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

    /**
     * Aggregates system-wide compliance statistics.
     * * @param metrics Map to populate with regulatory compliance KPIs.
     */
    private void calculateComplianceMetrics(Map<String, Object> metrics) {
        long totalRecords = complianceRepository.count();
        long passedRecords = complianceRepository.findAll().stream()
                .filter(r -> r.getResult() == ComplianceResult.PASSED)
                .count();

        double passRate = totalRecords > 0 ? (double) passedRecords / totalRecords * 100 : 0;

        metrics.put("overallCompliancePassRate", String.format(PERCENT_FORMAT, passRate));
        metrics.put("totalEntitiesAudited", totalRecords);
    }
}