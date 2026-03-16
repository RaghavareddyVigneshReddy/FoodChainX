package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

import com.cts.foodchainx.enums.ShipmentStatus;

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
    @Column(name="Shipment_ID")
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="Batch_ID", nullable=false) 
    private ProductionBatch batch; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="Distributor_ID", nullable=false) 
    private User distributor; 

    @Column(name="Departure_date", nullable=false)
    private LocalDate departureDate;

    @Column(name="Arrival_Date")
    private LocalDate arrivalDate;

    @Enumerated(EnumType.STRING)
    @Column(name="Status", length=50)
    private ShipmentStatus status;
}