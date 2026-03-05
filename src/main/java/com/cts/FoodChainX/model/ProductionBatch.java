package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
@Entity
@Table(name = "PRODUCTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BatchID")
    private Long productionId;

    // Relationship instead of private Integer farmId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FarmID", nullable = false)
    private Farm farm;

    @Column(name = "CropType", length = 100, nullable = false)
    private String cropType;

    @Column(name = "Quantity", nullable = false)
    private Double quantity;

    @Column(name = "HarvestDate", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "QualityStatus", length = 50, nullable = false)
    private String qualityStatus;
    // Inside ProductionBatch.java

@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
// This list is what provides the 'getQualityChecks()' method you need
private List<QualityCheck> qualityChecks;
}