package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Sale;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {

    List<Sale> findByInventoryID(Integer inventoryID);

    List<Sale> findByConsumerID(Integer consumerID);
}
