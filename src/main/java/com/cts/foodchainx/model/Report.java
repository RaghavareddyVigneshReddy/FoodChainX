package com.cts.foodchainx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Persistence entity representing a generated Supply Chain Report.
 * <p>
 * This class maps to the {@code REPORT} table in the database and stores 
 * historical snapshots of system performance metrics for audit and regulatory review.
 * </p>
 * * @author FoodChainX Development Team
 * @version 1.0
 */
@Entity
@Table(name = "REPORT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    /**
     * The unique identifier for the report record.
     * Automatically generated using the Identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Report_ID")
    private Long reportId;

    /**
     * The boundary of the report's data (e.g., "GLOBAL", "FARM", "RETAILER").
     * Defines the level of aggregation for the associated metrics.
     */
    @Column(name = "Scope", length = 255, nullable = false)
    private String scope;

    /**
     * A serialized representation of the report metrics (typically stored as JSON).
     * Contains key-value pairs of performance indicators such as compliance rates.
     */
    @Column(name = "Metrics", length = 255, nullable = false)
    private String metrics;

    /**
     * The date on which the report was finalized and persisted to the database.
     */
    @Column(name = "Generated_Date", nullable = false)
    private LocalDate generatedDate;
}