package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.report.ReportResponseDto;
import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.enums.ShipmentStatus;
import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.model.ProductionBatch;
import com.cts.foodchainx.model.Shipment;
import com.cts.foodchainx.repository.*;
import com.cts.foodchainx.serviceimpl.ReportServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ProductionBatchRepository batchRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private ComplianceRecordRepository complianceRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("Generate FARMER Report - Should calculate quality pass rate correctly")
    void generateScopedPerformance_Farmer_Success() {
        // Arrange
        ProductionBatch b1 = new ProductionBatch(); b1.setQualityStatus(QualityStatus.PASSED);
        ProductionBatch b2 = new ProductionBatch(); b2.setQualityStatus(QualityStatus.REJECTED);
        
        when(batchRepository.count()).thenReturn(2L);
        when(batchRepository.findAll()).thenReturn(List.of(b1, b2));
        when(complianceRepository.count()).thenReturn(0L); // Global metric

        // Act
        ReportResponseDto result = reportService.generateScopedPerformance("FARMER");

        // Assert
        assertEquals("FARMER", result.getScope());
        assertEquals("50.00%", result.getMetrics().get("harvestQualityPassRate"));
        assertEquals(2L, result.getMetrics().get("totalBatchesProduced"));
        verify(batchRepository, times(1)).count();
    }

    @Test
    @DisplayName("Generate DISTRIBUTOR Report - Should calculate delivery success rate")
    void generateScopedPerformance_Distributor_Success() {
        // Arrange
        Shipment s1 = new Shipment(); s1.setStatus(ShipmentStatus.DELIVERED);
        
        when(shipmentRepository.count()).thenReturn(1L);
        when(shipmentRepository.findAll()).thenReturn(List.of(s1));
        
        // Act
        ReportResponseDto result = reportService.generateScopedPerformance("DISTRIBUTOR");

        // Assert
        assertEquals("100.00%", result.getMetrics().get("deliverySuccessRate"));
        assertEquals(1L, result.getMetrics().get("activeShipmentsCount"));
    }

    @Test
    @DisplayName("Generate Report - Invalid Scope should throw IllegalArgumentException")
    void generateScopedPerformance_InvalidScope_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.generateScopedPerformance("INVALID_ROLE");
        });
    }

    @Test
    @DisplayName("Global Compliance Metrics - Should calculate pass rate for all reports")
    void calculateComplianceMetrics_Success() {
        // Arrange
        ComplianceRecord r1 = new ComplianceRecord(); r1.setResult(ComplianceResult.PASSED);
        
        when(complianceRepository.count()).thenReturn(1L);
        when(complianceRepository.findAll()).thenReturn(List.of(r1));
        // Use an empty repo mock for the scope logic to avoid NPE
        when(batchRepository.count()).thenReturn(0L);

        // Act
        ReportResponseDto result = reportService.generateScopedPerformance("FARMER");

        // Assert
        assertEquals("100.00%", result.getMetrics().get("overallCompliancePassRate"));
        assertEquals(1L, result.getMetrics().get("totalEntitiesAudited"));
    }
}