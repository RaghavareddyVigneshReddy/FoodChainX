package com.cts.FoodChainX.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "SALE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleID")
    private Long saleId;

    @Column(name = "InventoryID", nullable = false)
    private Long inventoryId;

    @Column(name = "ConsumerID", nullable = false)
    private Long consumerId;

    @Column(name = "BatchID", nullable = false)
    private Long batchId;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Quantity", nullable = false)
    private Long quantity;

    @Column(name = "Price", nullable = false)
    private Double price;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InventoryID", insertable = false, updatable = false)
    @JsonIgnore
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConsumerID", insertable = false, updatable = false)
    @JsonIgnore
    private User consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID", insertable = false, updatable = false)
    @JsonIgnore
    private ProductionBatch productionBatch;
}
