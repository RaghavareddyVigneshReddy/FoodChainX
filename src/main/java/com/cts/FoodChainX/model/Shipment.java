package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "SHIPMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShipmentID")
    private int shipmentId;

    @Column(name = "BatchID", nullable = false)
    private int batchId;

    @Column(name = "DistributorID", nullable = false)
    private int distributorId;

    @Column(name = "DepartureDate")
    private LocalDate departureDate;

    @Column(name = "ArrivalDate")
    private LocalDate arrivalDate;

    @Column(name = "Status")
    private String status;

    @PrePersist
    protected void onCreate() {
        // Default status if not provided
        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }
    }
}