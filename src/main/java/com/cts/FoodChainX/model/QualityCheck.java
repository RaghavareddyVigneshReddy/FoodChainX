package com.cts.FoodChainX.model;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "QUALITY_CHECK")
public class QualityCheck{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QualityID")
    private Integer qualityId;
    @Column(name = "BatchID", nullable = false)
    private Integer batchId;
    @Column(name = "InspectorID", nullable = false)
    private Integer inspectorId;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Lob
    @Column(name = "Findings")
    private String findings;

    @Column(name = "Status", length = 50)
    private String status;
}