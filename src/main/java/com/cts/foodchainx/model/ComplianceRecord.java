package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.ComplianceType;

/**
 * Entity representing a regulatory compliance record within the FoodChainX system.
 * <p>
 * This model captures the outcome of a specific audit performed on a supply chain 
 * participant. It serves as the primary data source for the Compliance & Audit 
 * Management module and informs the safety status in the Consumer Portal.
 * </p>
 */
@Entity
@Table(name = "COMPLIANCE_RECORD")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRecord {

    /**
     * Unique identifier for the compliance record.
     * Maps to the primary key 'ComplianceID' in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Compliance_ID")
    private Long complianceId;

    /**
     * The ID of the specific entity being audited.
     * This links the record to a Farm, Distributor, or Retailer.
     */
    @Column(name = "Entity_ID", nullable = false)
    private Long entityId;

    /**
     * The category of the entity being audited.
     * Stored as a string representation of {@link ComplianceType}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 100, nullable = false)
    private ComplianceType type;

    /**
     * The final outcome of the compliance audit.
     * Stored as a string representation of {@link ComplianceResult}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Result", length = 100, nullable = false)
    private ComplianceResult result;

    /**
     * The date on which the compliance audit was conducted.
     * Automatically populated during record creation in the service layer.
     */
    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    /**
     * Detailed observations or remarks provided by the regulator during the audit.
     * Maps to a TEXT column in the database to support long-form findings.
     */
    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;
}