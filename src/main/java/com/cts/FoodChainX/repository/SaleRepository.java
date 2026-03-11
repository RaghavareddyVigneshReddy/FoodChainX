package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Sale;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByInventoryId(Long inventoryId);

    List<Sale> findByConsumerId(Long consumerId);
}
