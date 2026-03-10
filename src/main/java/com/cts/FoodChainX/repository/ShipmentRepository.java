package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Corrected: Matches 'private Long distributorId' in Shipment model
    List<Shipment> findByDistributorId(Long distributorId);

    // Corrected: Matches 'private Long batchId' in Shipment model
    List<Shipment> findByBatchId(Long batchId);

    // This was already correct as it matches 'private String status'
    List<Shipment> findByStatus(String status);

    // Corrected: Matches 'distributorId' and 'departureDate'
    List<Shipment> findByDistributorIdOrderByDepartureDateDesc(Long distributorId);

    // This was already correct
    List<Shipment> findByDistributorIdAndStatus(Long distributorId, String status);
}