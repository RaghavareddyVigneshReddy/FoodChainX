package com.cts.FoodChainX.model;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    private Integer warehouseId;

    @Column(name = "WarehouseName") // Added for traceability display
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DistributorID", nullable = false)
    private User distributor;

    @Column(name = "Location", length = 255, nullable = false)
    private String location;

    @Column(name = "Capacity", nullable = false)
    private Integer capacity;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;
}