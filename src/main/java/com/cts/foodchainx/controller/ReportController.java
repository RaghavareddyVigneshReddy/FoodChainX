package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.report.ReportResponseDto;
import com.cts.foodchainx.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing and retrieving Supply Chain Reports.
 * <p>
 * This controller provides endpoints for system-wide performance analytics,
 * ensuring that sensitive data is only accessible to authorized personnel.
 * </p>
 * * @author FoodChainX Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Retrieves the global supply chain performance dashboard.
     * <p>
     * This endpoint calculates real-time metrics including total production batches 
     * and the overall compliance rate. Access is restricted to users with 
     * ADMIN or REGULATOR roles.
     * </p>
     * * @return {@link ResponseEntity} containing the {@link ReportResponseDto} with performance metrics
     * @throws org.springframework.security.access.AccessDeniedException if the user lacks the required roles
     */
    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public ResponseEntity<ReportResponseDto> getPerformanceDashboard(@RequestParam(defaultValue = "FARM") String scope) {
        log.info("REST request for {} Performance Dashboard", scope);
        return ResponseEntity.ok(reportService.generateScopedPerformance(scope));
    }
}