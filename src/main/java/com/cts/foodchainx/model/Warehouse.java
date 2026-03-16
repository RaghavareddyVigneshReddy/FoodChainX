package com.cts.foodchainx.model;

import jakarta.persistence.*;
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
    @Column(name = "WarehouseID")
    private Long warehouseId;

    @Column(name = "WarehouseName")
    private String name;

    /** The distributor user who manages this warehouse */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "DistributorID", nullable = false) 
    private User distributor; 

    @Column(name = "Location", length = 255, nullable = false)
    private String location;

    @Column(name = "Capacity", nullable = false)
    private Long capacity;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;
}