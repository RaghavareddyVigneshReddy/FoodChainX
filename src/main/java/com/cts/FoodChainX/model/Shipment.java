package com.cts.FoodChainX.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
<<<<<<< HEAD
=======

import java.time.LocalDate;
>>>>>>> 1d34a87aa1148910c85de137ade3d1d56eeecd20
import java.time.LocalDateTime;
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
<<<<<<< HEAD
    @JoinColumn(name="BatchID",nullable="false")
    private ProductionBatch batch;
    @ManyToOne
    @JoinColumn(name="DistributorID",nullable=false)
    private user distributor;
=======
    @JoinColumn(name="BatchID",nullable=false)
    private Integer batch;
    @ManyToOne
    @JoinColumn(name="DistributorID",nullable=false)
    private Integer distributor;
>>>>>>> 1d34a87aa1148910c85de137ade3d1d56eeecd20
    @Column(name="Departuredate",nullable=false)
    private LocalDate departureDate;
    @Column(name="ArrivalDate")
    private LocalDate arrivalDate;
    @Column(name="Status",length=50)
    private String status;
  

    

}