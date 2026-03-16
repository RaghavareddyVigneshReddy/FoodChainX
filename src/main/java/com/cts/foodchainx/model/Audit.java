package com.cts.foodchainx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.cts.foodchainx.enums.AuditStatus;

/**
 * Entity representing a formal regulatory audit process within the FoodChainX platform.
 * <p>
 * This model serves as a high-level container for a Regulator's inspection campaign. 
 * It tracks the overall scope, general findings, and lifecycle status of the audit 
 * process, distinguishing the administrative procedure from individual compliance records.
 * </p>
 */
@Entity
@Table(name = "AUDIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audit {

    /**
     * Unique identifier for the audit record.
     * Maps to the primary key 'AuditID' in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Audit_ID")
    private Long auditId;

    /**
     * The ID of the Regulator responsible for conducting this audit.
     * Links back to the User entity with the 'REGULATOR' role.
     */
    @Column(name = "Regulator_ID", nullable = false)
    private Long regulatorId;

    /**
     * The defined boundary or theme of the audit.
     * Example: "Organic Farm Review Q1" or "Regional Distribution Check".
     */
    @Column(name = "Scope", nullable = false, length = 50)
    private String scope;

    /**
     * A summary of observations and conclusions reached during the audit.
     * Provides a high-level narrative of the regulatory findings.
     */
    @Column(name = "Findings", nullable = false, length = 500)
    private String findings;

    /**
     * The date on which the audit record was initiated or performed.
     */
    @Column(name = "Date", nullable = false)
    private LocalDate date;

    /**
     * The current lifecycle stage of the audit.
     * Stored as a string representation of {@link AuditStatus} (e.g., OPEN, CLOSED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 30)
    private AuditStatus status;
}