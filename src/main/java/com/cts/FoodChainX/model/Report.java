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
@Table(name = "REPORT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Long reportId;

    @Column(name = "Scope", length = 255, nullable = false)
    private String scope;

    @Column(name = "Metrics", length = 255, nullable = false)
    private String metrics;

    @Column(name = "GeneratedDate", nullable = false)
    private LocalDate generatedDate;
}
