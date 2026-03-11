package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Inventory;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByRetailerId(Long retailerId);

    List<Inventory> findByBatchId(Long batchId);

    List<Inventory> findByStatus(String status);
}