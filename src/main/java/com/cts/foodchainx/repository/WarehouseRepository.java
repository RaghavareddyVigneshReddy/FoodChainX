package com.cts.foodchainx.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.foodchainx.model.Warehouse;

/**
 * Data access layer for {@link Warehouse} entities.
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /** Finds all warehouses managed by a specific distributor. */
    List<Warehouse> findByDistributor_UserId(Long distributorId);

    /** Finds warehouses based on their operational status (e.g., 'Full'). */
    List<Warehouse> findByStatus(String status);

    /** Filters warehouses for a distributor by status. */
    List<Warehouse> findByDistributor_UserIdAndStatus(Long distributorId, String status);

    /** Lists warehouses for a distributor ordered by storage capacity. */
    List<Warehouse> findByDistributor_UserIdOrderByCapacityDesc(Long distributorId);
}