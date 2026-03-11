package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Reaches into the 'distributor' (User) object to find 'userId'
    List<Shipment> findByDistributor_UserId(Long userId);

    // Reaches into the 'batch' (ProductionBatch) object to find 'productionId'
    List<Shipment> findByBatch_ProductionId(Long productionId);

    // This remains the same as 'status' is a direct field in Shipment
    List<Shipment> findByStatus(String status);

    // Reaches into distributor and orders by date
    List<Shipment> findByDistributor_UserIdOrderByDepartureDateDesc(Long userId);

    // Reaches into distributor and filters by status
    List<Shipment> findByDistributor_UserIdAndStatus(Long userId, String status);
}