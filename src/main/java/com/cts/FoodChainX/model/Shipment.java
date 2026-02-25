package com.cts.FoodChainX.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.time.LocalDate;

@Entity
@Table(name="SHIPMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ShipmentID")
    private Integer shipmentID;
    @ManyToOne
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
    private String status;
  
}