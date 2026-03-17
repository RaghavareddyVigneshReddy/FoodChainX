package com.cts.foodchainx.model;

import com.cts.foodchainx.enums.WarehouseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistence entity representing a storage facility within the FoodChainX logistics network.
 * This class maps to the 'WAREHOUSE' table and tracks storage capacity and stock levels 
 * managed by distributors.
 */
@Entity
@Table(name = "WAREHOUSE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    /** * Unique identifier for the warehouse.
     * Automatically generated using the database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Warehouse_ID")
    private Long warehouseId;

    /** * Descriptive name of the warehouse facility.
     */
    @Column(name = "Warehouse_Name")
    private String name;

    /** * The Distributor (User) who owns or manages this facility.
     * Established as a Lazy-loaded Many-to-One relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "Distributor_ID", nullable = false) 
    private User distributor; 

    /** * Physical address or geographic location of the warehouse.
     */
    @Column(name = "Location", length = 255, nullable = false)
    private String location;

    /** * The total storage volume or units the warehouse can hold.
     */
    @Column(name = "Capacity", nullable = false)
    private Long capacity;

    /** * The current amount of stock stored in the facility.
     * Initialized to 0.0 and represented as a Double to accommodate decimal quantities.
     */
    @Column(name = "Current_Stock_Level", nullable = false)
    private Double currentStockLevel = 0.0;

    /** * Operational status of the warehouse (e.g., AVAILABLE, FULL, MAINTENANCE).
     * Persisted as a String for better database readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50, nullable = false)
    private WarehouseStatus status;
}