package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Inventory;
import org.springframework.lang.NonNull;
import java.util.List;

/**
 * Service interface for managing {@link Inventory} operations within the FoodChainX system.
 * This service provides methods for creating, retrieving, and filtering inventory records
 * associated with various retailers.
 */
public interface InventoryService {

    /**
     * Creates and persists a new inventory record.
     *
     * @param inventory The {@link Inventory} object containing the details to be saved.
     * @return The saved {@link Inventory} object, typically including its generated ID.
     */
    Inventory createInventory(Inventory inventory);

    /**
     * Retrieves all inventory records present in the system.
     *
     * @return A {@link List} of all {@link Inventory} objects; returns an empty list if none exist.
     */
    List<Inventory> getAllInventory();

    /**
     * Finds a specific inventory record by its unique identifier.
     *
     * @param inventoryId The unique ID of the inventory to retrieve. Must not be null.
     * @return The found {@link Inventory} object.
     * @throws RuntimeException (or specific EntityNotFoundException) if no inventory exists with the given ID.
     */
    Inventory getInventoryById(@NonNull Long inventoryId);

    /**
     * Retrieves all inventory items associated with a specific retailer.
     *
     * @param retailerId The unique ID of the retailer whose inventory is being queried. Must not be null.
     * @return A {@link List} of {@link Inventory} objects belonging to the specified retailer.
     */
    List<Inventory> getInventoryByRetailer(@NonNull Long retailerId);

}