package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    
    List<Shipment> findByDistributor(Integer distributor);

    
    List<Shipment> findByBatch(Integer batch);

    List<Shipment> findByStatus(String status);

    
    List<Shipment> findByDistributorOrderByDepartureDateDesc(Integer distributor);

    List<Shipment> findByDistributorAndStatus(Integer distributor, String status);
}