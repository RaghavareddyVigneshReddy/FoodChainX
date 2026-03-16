package com.cts.foodchainx.model;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;

import com.cts.foodchainx.enums.WarehouseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistence entity representing a storage facility in the supply chain.
 * Maps to the 'WAREHOUSE' database table and manages storage capacity for distributors.
 */
@Entity
@Table(name = "WAREHOUSE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Warehouse_ID")
    private Long warehouseId;

    @Column(name = "Warehouse_Name")
    private String name;

    /** The distributor user who manages this warehouse */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "Distributor_ID", nullable = false) 
    private User distributor; 

    @Column(name = "Location", length = 255, nullable = false)
    private String location;

    @Column(name = "Capacity", nullable = false)
    private Long capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50, nullable = false)
    private WarehouseStatus status;
}