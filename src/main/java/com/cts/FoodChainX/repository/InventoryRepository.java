package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Inventory;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    List<Inventory> findByRetailerId(Integer retailerId);

    List<Inventory> findByBatchId(Integer batchId);

    List<Inventory> findByStatus(String status);
}