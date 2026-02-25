package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SHIPMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShipmentID")
    private Integer shipmentID;

    @Column(name = "BatchID")
    private Integer batchID;

    @Column(name = "DistributorID")
    private Integer distributorID;

    @Column(name = "DepartureDate")
    private java.sql.Date departureDate;

    @Column(name = "ArrivalDate")
    private java.sql.Date arrivalDate;

    @Column(name = "Status", length = 50)
    private String status;
}