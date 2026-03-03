package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    private Integer productionId;

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
}