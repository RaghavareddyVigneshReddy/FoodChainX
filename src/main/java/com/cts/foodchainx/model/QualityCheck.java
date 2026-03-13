package com.cts.foodchainx.model;
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
    @Column(name = "qualityid") // MySQL is case-insensitive, but let's match the table
    private Long qualityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchid", nullable = false) // Fix: was batch_id
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspectorid", nullable = false) // Fix: was inspector_id
    private User inspector; 

    @Column(name = "inspection_date", nullable = false)
    private LocalDate date;

    @Lob
    @Column(name = "findings")
    private String findings;

    @Column(name = "status", length = 50)
    private String status;
}