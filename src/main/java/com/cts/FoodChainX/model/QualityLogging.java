package com.cts.FoodChainX.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "QUALITY_CHECK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityLogging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QualityID")
    private Integer qualityId;

    // Foreign keys as scalars (no cardinality mapping)
    @Column(name = "BatchID")
    private Integer batchId;

    @Column(name = "InspectorID")
    private Integer inspectorId;

    @Column(name = "Date")
    private LocalDate date;

    @Lob
    @Column(name = "Findings")
    private String findings;

    @Column(name = "Status", length = 50)
    private String status;
}
