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
    @Column(name = "QualityID")
    private Long qualityId;

    // Keeping FK as scalar (no relation mapping)
    @Column(name = "BatchID", nullable = false)
    private Long batchId;

    // Keeping FK as scalar (no relation mapping)
    @Column(name = "InspectorID", nullable = false)
    private Long inspectorId;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Lob
    @Column(name = "Findings")
    private String findings;

    @Column(name = "Status", length = 50)
    private String status;
}