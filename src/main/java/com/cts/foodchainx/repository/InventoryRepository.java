package com.cts.foodchainx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.enums.InventoryStatus;
import com.cts.foodchainx.model.Inventory;
import java.util.List;

/**
 * Data Access Object (DAO) for {@link Inventory} entities.
 * <p>
 * This interface leverages Spring Data JPA's query derivation mechanism to
 * automatically generate SQL queries based on method signatures.
 * </p>
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Retrieves all inventory records assigned to a specific retailer.
     * * @param retailerId The unique ID of the retailer.
     * @return A list of {@link Inventory} records matching the retailer ID.
     */
    List<Inventory> findByRetailerId(Long retailerId);

    /**
     * Finds inventory records associated with a specific production batch.
     *
     * @param batchId The unique ID of the production batch.
     * @return A list of {@link Inventory} items originating from the specified batch.
     */
    List<Inventory> findByBatchId(Long batchId);

    /**
     * Filters inventory records based on their current availability status.
     * <p>
     * Useful for identifying items that are "OUT_OF_STOCK" or "LOW_STOCK"
     * across the entire retail network.
     * </p>
     *
     * @param status The status string (e.g., "AVAILABLE", "LOW_STOCK").
     * @return A list of {@link Inventory} items matching the status.
     */
    List<Inventory> findByStatus(InventoryStatus status);
}