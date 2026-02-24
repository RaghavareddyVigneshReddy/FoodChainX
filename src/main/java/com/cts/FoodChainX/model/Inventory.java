package com.cts.FoodChainX.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Integer inventoryID;

    // Consider mapping as @ManyToOne if you have Retailer and Batch entities
    @Column(name = "RetailerID", nullable = false)
    private Integer retailerID;

    @Column(name = "BatchID", nullable = false)
    private Integer batchID;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "DateAdded", nullable = false)
    private LocalDate dateAdded;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;
}