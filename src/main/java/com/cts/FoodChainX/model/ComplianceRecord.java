package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "COMPLIANCE_RECORD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComplianceID")
    private Integer complianceID;

    // Could later be mapped to Supplier / Retailer / Entity table
    @Column(name = "EntityID", nullable = false)
    private Integer entityID;

    @Column(name = "Type", length = 100, nullable = false)
    private String type;

    @Column(name = "Result", length = 100, nullable = false)
    private String result;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;
}