package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.report.ReportResponseDto;

/**
 * Service interface for generating and managing system-wide performance analytics.
 * <p>
 * This service acts as the central intelligence hub for the FoodChainX platform, 
 * aggregating data from Production, Logistics, and Retail modules to provide 
 * transparency and oversight for Admins and Regulators.
 * </p>
 */
public interface ReportService {

    /**
     * Generates a real-time performance dashboard for a specific supply chain scope.
     * <p>
     * The report dynamically calculates Key Performance Indicators (KPIs) such as 
     * Quality Pass Rates, Delivery Efficiency, and Traceability Verification percentages 
     * based on the provided role-based scope.
     * </p>
     *
     * @param scope The organizational level to analyze (e.g., "FARMER", "DISTRIBUTOR", "RETAILER").
     * @return A {@link ReportResponseDto} containing the calculated metrics, scope, and generation timestamp.
     * @throws IllegalArgumentException if the provided scope is null or not recognized by the system.
     */
    public ReportResponseDto generateScopedPerformance(String scope);

    /**
     * Executes the daily archival process to persist system performance snapshots.
     * <p>
     * This method is typically triggered by a scheduled task at the end of a business cycle 
     * to ensure historical data is preserved for long-term trend analysis and regulatory compliance.
     * </p>
     */
    public void archiveDailyReports();
}