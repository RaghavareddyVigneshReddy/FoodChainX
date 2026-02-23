package com.cts.FoodChainX.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TRACE_RECORD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TraceID")
    private Integer traceID;

    @Column(name = "BatchID", nullable = false)
    private Integer batchID; // FK to BATCH

    @Column(name = "FarmID", nullable = false)
    private Integer farmID; // FK to FARM

    @Column(name = "DistributorID", nullable = false)
    private Integer distributorID; // FK to DISTRIBUTOR

    @Column(name = "RetailerID", nullable = false)
    private Integer retailerID; // FK to RETAILER

    @Column(name = "ConsumerID", nullable = false)
    private Integer consumerID; // FK to USER (Consumer role)

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Status", length = 255)
    private String status;
}
