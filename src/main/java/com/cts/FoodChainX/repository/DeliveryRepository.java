package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    
    List<Delivery> findByShipmentId(Long shipmentId);

    
    List<Delivery> findByRetailerId(Long retailerId);

    
    List<Delivery> findByStatus(String status);

    
    List<Delivery> findByRetailerIdOrderByDateDesc(Long retailerId);

    
    List<Delivery> findByRetailerIdAndStatus(Long retailerId, String status);
}