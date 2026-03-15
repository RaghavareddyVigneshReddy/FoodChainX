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

    @Column(name = "WarehouseName") // Added for traceability display
    private String name;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "DistributorID", nullable = false) 
    private User distributor; 

    @Column(name = "Location", length = 255, nullable = false)
    private String location;

    @Column(name = "Capacity", nullable = false)
    private Long capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50, nullable = false)
    private WarehouseStatus status;
}