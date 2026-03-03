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

@Entity
@Table(name = "TRACE_RECORD")
@Data
public class TraceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TraceID")
    private Integer traceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID")
    private ProductionBatch productionBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FarmID")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DistributorID")
    private Warehouse distributor;

    @Column(name = "RetailerID")
    private Integer retailerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConsumerID")
    private User consumer;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "Status")
    private String status;
}