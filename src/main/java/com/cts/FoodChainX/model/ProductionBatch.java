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
    @Column(name = "BatchID") // If your PK is named differently, update here
    private Integer productionId;

    // FK kept as scalar (no @ManyToOne)
    @Column(name = "FarmID", nullable = false)
    private Integer farmId;

    @Column(name = "CropType", length = 100, nullable = false)
    private String cropType;

    // Use Double for FLOAT; switch to BigDecimal if your DB uses DECIMAL
    @Column(name = "Quantity", nullable = false)
    private Double quantity;

    @Column(name = "HarvestDate", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "QualityStatus", length = 50, nullable = false)
    private String qualityStatus;
}
