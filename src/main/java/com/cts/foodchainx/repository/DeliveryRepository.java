package com.cts.foodchainx.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.foodchainx.model.Delivery;

/**
 * Data access layer for {@link Delivery} entities.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /** Finds deliveries associated with a specific shipment ID. */
    List<Delivery> findByShipment_ShipmentId(Long shipmentId);

    /** Finds all deliveries received by a specific retailer. */
    List<Delivery> findByRetailer_UserId(Long retailerId);

    /** Filters deliveries by their current status. */
    List<Delivery> findByStatus(String status);

    /** Retrieves delivery history for a retailer, sorted by most recent date. */
    List<Delivery> findByRetailer_UserIdOrderByDateDesc(Long retailerId);

    /** Finds deliveries for a retailer filtered by status. */
    List<Delivery> findByRetailer_UserIdAndStatus(Long retailerId, String status);
}