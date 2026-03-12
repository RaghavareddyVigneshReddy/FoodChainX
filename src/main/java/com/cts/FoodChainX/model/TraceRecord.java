package com.cts.FoodChainX.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Persistence entity representing a single point of traceability in the supply chain.
 * Each record captures a "checkpoint" for a specific production batch, documenting 
 * the movement of goods from the farm through distributors and retailers to the final consumer.
 * * <p>This entity is central to the Consumer Transparency Portal, allowing for 
 * a full audit trail of a product's journey.</p>
 */
@Entity
@Table(name = "TRACE_RECORD")
@Data
public class TraceRecord {

    /**
     * Unique identifier for the trace record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TraceID")
    private Long traceId;

    /**
     * The specific production batch associated with this trace entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID")
    private ProductionBatch productionBatch;

    /**
     * The farm where the product batch originated.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FarmID")
    private Farm farm;

    /**
     * The user acting as the distributor for the batch at this point in the chain.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DistributorID") 
    private User distributor; 

    /**
     * The user acting as the retailer who received the batch.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RetailerID")
    private User retailer;

    /**
     * The end consumer who purchased the batch, if applicable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConsumerID")
    private User consumer;

    /**
     * The date on which this traceability event occurred or was recorded.
     */
    @Column(name = "Date")
    private LocalDate date;

    /**
     * The current status of the batch (e.g., HARVESTED, IN_TRANSIT, DELIVERED).
     */
    @Column(name = "Status")
    private String status;
}