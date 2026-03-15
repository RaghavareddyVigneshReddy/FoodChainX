package com.cts.foodchainx.model;

import java.time.LocalDate;

import com.cts.foodchainx.enums.ShipmentStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistence entity representing the finalized delivery of goods to a retailer.
 * Maps to the 'DELIVERY' database table.
 */
@Entity
@Table(name = "DELIVERY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DeliveryID")
    private Long deliveryId;

    /** The shipment associated with this specific delivery hand-off */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "ShipmentID", nullable = false) 
    private Shipment shipment; 

    /** The retail user receiving the delivery */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "RetailerID", nullable = false) 
    private User retailer; 

    @Column(name = "Date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private ShipmentStatus status;
}