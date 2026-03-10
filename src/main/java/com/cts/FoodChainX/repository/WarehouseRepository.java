package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    
    List<Warehouse> findByDistributorId(Long distributorId);

    
    List<Warehouse> findByStatus(String status);

    
    List<Warehouse> findByDistributorIdAndStatus(Long distributorId, String status);

    
    List<Warehouse> findByDistributorIdOrderByCapacityDesc(Long distributorId);
}
