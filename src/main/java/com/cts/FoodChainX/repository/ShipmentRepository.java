package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    // Matches 'private Integer distributor'
    List<Shipment> findByDistributor(Integer distributor);

    // Matches 'private Integer batch'
    List<Shipment> findByBatch(Integer batch);

    List<Shipment> findByStatus(String status);

    // Matches 'private Integer distributor' + 'private LocalDate departureDate'
    List<Shipment> findByDistributorOrderByDepartureDateDesc(Integer distributor);

    List<Shipment> findByDistributorAndStatus(Integer distributor, String status);
}