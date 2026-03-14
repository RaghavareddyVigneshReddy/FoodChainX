package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Entity representing a Shipment within the supply chain.
 * Maps to the SHIPMENT table and tracks the movement of a ProductionBatch.
 */
@Entity
@Table(name="SHIPMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ShipmentID")
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="BatchID", nullable=false) 
    private ProductionBatch batch; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="DistributorID", nullable=false) 
    private User distributor; 

    @Column(name="Departuredate", nullable=false)
    private LocalDate departureDate;

    @Column(name="ArrivalDate")
    private LocalDate arrivalDate;

    @Column(name="Status", length=50)
    private String status;
}