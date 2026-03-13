package com.cts.foodchainx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "INVENTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryID")
    private Long inventoryId;

    @Column(name = "RetailerID", nullable = false)
    private Long retailerId;

    @Column(name = "BatchID", nullable = false)
    private Long batchId;

    @Column(name = "Quantity", nullable = false)
    private Long quantity;

    @Column(name = "DateAdded", nullable = false)
    private LocalDate dateAdded;

    @Column(name = "Status", nullable = false)
    private String status;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RetailerID", insertable = false, updatable = false)
    @JsonIgnore
    private User retailer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID", insertable = false, updatable = false)
    @JsonIgnore
    private ProductionBatch productionBatch;
}
