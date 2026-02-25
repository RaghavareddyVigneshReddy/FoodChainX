package com.cts.FoodChainX.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
<<<<<<< HEAD
=======

import java.time.LocalDate;
>>>>>>> dbcfc28c2d520a72c2ebad2ccfe6a76f376ab71c

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name="SHIPMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
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
  

    

=======
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
>>>>>>> dbcfc28c2d520a72c2ebad2ccfe6a76f376ab71c
}