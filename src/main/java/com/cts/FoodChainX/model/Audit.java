package com.cts.FoodChainX.model;

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

@Entity
@Table(name = "AUDIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AuditID")
    private Long auditId;

    @Column(name = "RegulatorID", nullable = false)
    private Long regulatorId;

    @Column(name = "Scope", nullable = false, length = 50)
    private String scope;

    @Column(name = "Findings", nullable = false, length = 500)
    private String findings;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Status", nullable = false, length = 30)
    private String status;
}