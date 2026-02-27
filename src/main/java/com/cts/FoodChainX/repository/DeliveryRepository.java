package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    
    List<Delivery> findByShipmentId(Integer shipmentId);

    
    List<Delivery> findByRetailerId(Integer retailerId);

    
    List<Delivery> findByStatus(String status);

    
    List<Delivery> findByRetailerIdOrderByDateDesc(Integer retailerId);

    
    List<Delivery> findByRetailerIdAndStatus(Integer retailerId, String status);
}