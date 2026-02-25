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
    
    @JoinColumn(name="BatchID",nullable=false)
    private Integer batch;
    @ManyToOne
    @JoinColumn(name="DistributorID",nullable=false)
    private Integer distributor;
    @Column(name="Departuredate",nullable=false)
    private LocalDate departureDate;
    @Column(name="ArrivalDate")
    private LocalDate arrivalDate;
    @Column(name="Status",length=50)

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