package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    
    List<Shipment> findByDistributorId(int distributorId);

    
    List<Shipment> findByBatchId(int batchId);

    
    List<Shipment> findByStatus(String status);

    
    List<Shipment> findByDistributorIdOrderByDepartureDateDesc(int distributorId);

    
    List<Shipment> findByDistributorIdAndStatus(int distributorId, String status);
}