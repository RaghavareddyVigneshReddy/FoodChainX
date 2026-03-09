package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.report.ReportResponseDto;
import com.cts.FoodChainX.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public ResponseEntity<ReportResponseDto> getPerformanceDashboard() {
        log.info("REST request for Supply Chain Performance Dashboard");
        return ResponseEntity.ok(reportService.generateSupplyChainPerformance());
    }
}