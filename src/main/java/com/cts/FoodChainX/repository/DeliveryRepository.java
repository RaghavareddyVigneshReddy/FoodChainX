package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // Navigates: Delivery -> Shipment -> shipmentId
    List<Delivery> findByShipment_ShipmentId(Long shipmentId);

    // Navigates: Delivery -> User (retailer) -> userId
    List<Delivery> findByRetailer_UserId(Long retailerId);

    // Direct property on Delivery
    List<Delivery> findByStatus(String status);

    // Navigates to retailer's userId and sorts by Delivery date
    List<Delivery> findByRetailer_UserIdOrderByDateDesc(Long retailerId);

    // Navigates to retailer's userId and filters by Delivery status
    List<Delivery> findByRetailer_UserIdAndStatus(Long retailerId, String status);
}