package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    
    List<Delivery> findByShipmentId(int shipmentId);

    
    List<Delivery> findByRetailerId(int retailerId);

    
    List<Delivery> findByStatus(String status);

    
    List<Delivery> findByRetailerIdOrderByDateDesc(int retailerId);

    
    List<Delivery> findByRetailerIdAndStatus(int retailerId, String status);
}