package com.cts.FoodChainX.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.FoodChainX.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    
    List<Warehouse> findByDistributorId(Integer distributorId);

    
    List<Warehouse> findByStatus(String status);

    
    List<Warehouse> findByDistributorIdAndStatus(Integer distributorId, String status);

    
    List<Warehouse> findByDistributorIdOrderByCapacityDesc(Integer distributorId);
}
