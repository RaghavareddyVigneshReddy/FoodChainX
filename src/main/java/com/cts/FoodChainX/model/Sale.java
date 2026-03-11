package com.cts.FoodChainX.model;

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

    // Consider @ManyToOne mapping if Inventory is an entity
    @Column(name = "InventoryID", nullable = false)
    private Long inventoryId;

    // Consumer reference (can also be @ManyToOne if you have Consumer entity)
    @Column(name = "ConsumerID", nullable = false)
    private Long consumerId;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Quantity", nullable = false)
    private Long quantity;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "BatchID", nullable = false)
    private Long batchId;
}