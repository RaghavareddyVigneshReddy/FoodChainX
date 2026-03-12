package com.cts.foodchainx.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.foodchainx.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    // Navigates: Warehouse -> User (distributor) -> userId
    List<Warehouse> findByDistributor_UserId(Long distributorId);

    // Direct property on Warehouse
    List<Warehouse> findByStatus(String status);

    // Navigates to User and filters by status
    List<Warehouse> findByDistributor_UserIdAndStatus(Long distributorId, String status);

    // Navigates to User and orders by Warehouse capacity
    List<Warehouse> findByDistributor_UserIdOrderByCapacityDesc(Long distributorId);
}