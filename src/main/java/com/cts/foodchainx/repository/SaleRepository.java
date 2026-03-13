package com.cts.foodchainx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.foodchainx.model.Sale;
import java.util.List;

/**
 * Data Access Object (DAO) for {@link Sale} entities.
 * <p>
 * This interface provides automated query generation for sales transactions,
 * allowing for rapid retrieval of sales data by inventory, consumer, or production batch.
 * </p>
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Retrieves all sales associated with a specific inventory record.
     * * @param inventoryId The unique ID of the inventory item.
     * @return A list of {@link Sale} records linked to the specified inventory.
     */
    List<Sale> findByInventoryId(Long inventoryId);

    /**
     * Finds the purchase history for a specific consumer.
     * * @param consumerId The unique ID of the user (Consumer).
     * @return A list of {@link Sale} transactions performed by the consumer.
     */
    List<Sale> findByConsumerId(Long consumerId);

    /**
     * Retrieves all sales records for a specific production batch.
     * <p>
     * This is a critical method for food safety recalls, allowing the system
     * to identify every consumer who purchased a product from a specific batch.
     * </p>
     * * @param batchId The unique ID of the production batch.
     * @return A list of {@link Sale} records belonging to the production batch.
     */
    List<Sale> findByBatchId(Long batchId);
}