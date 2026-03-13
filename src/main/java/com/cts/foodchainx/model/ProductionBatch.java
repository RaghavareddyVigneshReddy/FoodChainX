package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity representing a specific harvest unit (Batch) in the production cycle.
 * <p>This class acts as the central hub of the supply chain, connecting a Farm 
 * to its harvested crops and subsequent quality inspections.</p>
 */
@Entity
@Table(name = "PRODUCTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionBatch {

    /**
     * Unique identifier for the production batch.
     * Mapped to the 'BatchID' column in the 'PRODUCTION' table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BatchID")
    private Long productionId;

    /**
     * The Farm where this specific batch was harvested.
     * <p><b>Relationship:</b> Many-to-One association with {@link Farm}.</p>
     * <p><b>Fetch Type:</b> LAZY to optimize performance by not loading farm details 
     * until explicitly requested via getFarm().</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FarmID", nullable = false)
    private Farm farm;

    /**
     * The type of crop harvested (e.g., Wheat, Corn, Organic Apples).
     */
    @Column(name = "CropType", length = 100, nullable = false)
    private String cropType;

    /**
     * Total weight or volume of the harvested batch.
     */
    @Column(name = "Quantity", nullable = false)
    private Double quantity;

    /**
     * The date on which the crop was harvested.
     */
    @Column(name = "HarvestDate", nullable = false)
    private LocalDate harvestDate;

    /**
     * Current state of the batch in the QA lifecycle (e.g., PENDING, PASSED, REJECTED).
     */
    @Column(name = "QualityStatus", length = 50, nullable = false)
    private String qualityStatus;

    /**
     * A collection of all quality inspections performed on this specific batch.
     * <p><b>Relationship:</b> One-to-Many bidirectional mapping.</p>
     * <p><b>Cascade Type:</b> ALL - If a batch is deleted, all its associated 
     * quality logs will also be removed from the database.</p>
     */
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QualityCheck> qualityChecks;
}