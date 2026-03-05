package com.cts.FoodChainX.model;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "QUALITY_CHECK")
public class QualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_id")
    private Long qualityId;

    // Many quality checks can belong to one Production Batch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;

    // Many inspections can be done by one User (The Regulator)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector; 

    @Column(name = "inspection_date", nullable = false)
    private LocalDate date;

    @Lob
    @Column(name = "findings")
    private String findings;

    @Column(name = "status", length = 50)
    private String status;
}