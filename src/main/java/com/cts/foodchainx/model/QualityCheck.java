package com.cts.foodchainx.model;

import java.time.LocalDate;

import com.cts.foodchainx.enums.QualityStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Quality Inspection record for a production batch.
 * <p>This class stores the results of physical or chemical testing, the 
 * inspector responsible, and the final decision (status) for a specific batch.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "QUALITY_CHECK")
public class QualityCheck {

    /**
     * Unique identifier for the quality check record.
     * Mapped to 'qualityid' in the MySQL table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quality_id")
    private Long qualityId;

    /**
     * The specific production batch being inspected.
     * <p><b>Relationship:</b> Many-to-One association with {@link ProductionBatch}.</p>
     * <p><b>Database Join:</b> Linked via 'batchid'.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;

    /**
     * The User who performed the inspection (must have appropriate roles/permissions).
     * <p><b>Relationship:</b> Many-to-One association with {@link User}.</p>
     * <p><b>Database Join:</b> Linked via 'inspectorid'.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector; 

    /**
     * The date when the inspection was conducted.
     */
    @Column(name = "inspection_date", nullable = false)
    private LocalDate date;

    /**
     * Detailed notes and observations from the inspection.
     * <p><b>Annotation:</b> {@code @Lob} (Large Object) is used to allow for long, 
     * detailed text findings that might exceed standard VARCHAR limits.</p>
     */
    @Lob
    @Column(name = "findings")
    private String findings;

    /**
     * The outcome of the check (e.g., PASSED, REJECTED, NEEDS_RETEST).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private QualityStatus status;
}