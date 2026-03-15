package com.cts.foodchainx.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.foodchainx.enums.ShipmentStatus;
import com.cts.foodchainx.model.Shipment;

/**
 * Repository interface for managing {@link Shipment} entities.
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Finds all shipments associated with a specific distributor.
     * * @param userId the distributor's user ID
     * @return a list of shipments
     */
    List<Shipment> findByDistributor_UserId(Long userId);

    List<Shipment> findByBatch_ProductionId(Long productionId);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByDistributor_UserIdOrderByDepartureDateDesc(Long userId);

    List<Shipment> findByDistributor_UserIdAndStatus(Long userId, ShipmentStatus status);
}