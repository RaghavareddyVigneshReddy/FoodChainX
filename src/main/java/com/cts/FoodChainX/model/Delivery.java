package com.cts.FoodChainX.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DELIVERY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DeliveryID")
    private Integer deliveryID;

    
    @Column(name = "ShipmentID")
    private Integer shipmentID;

   
    @Column(name = "RetailerID")
    private Integer retailerID;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "Status", length = 50)
    private String status;
}