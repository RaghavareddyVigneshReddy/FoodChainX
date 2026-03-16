package com.cts.foodchainx.service;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.enums.InventoryStatus;
import com.cts.foodchainx.exception.InventoryNotFoundException;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.repository.InventoryRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class handling business logic for Retail Inventory management.
 * <p>
 * This service manages stock levels, automatically assigns inventory statuses
 * based on quantities, and ensures all additions are tracked via auditing.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    /**
     * Data access object for Inventory persistence.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * Saves a new inventory record and automatically calculates its status.
     * <p>
     * <b>Status Logic:</b>
     * <ul>
     * <li>Quantity = 0: OUT_OF_STOCK</li>
     * <li>Quantity ≤ 10: LOW_STOCK</li>
     * <li>Quantity > 10: AVAILABLE</li>
     * </ul>
     * This method is intercepted by {@link Auditable} for security logging.
     * </p>
     *
     * @param inventory The inventory entity to be persisted.
     * @return The saved {@link Inventory} entity with generated ID and status.
     */
    @Auditable(action = "ADD_RETAIL_INVENTORY", resource = "INVENTORY")
    public Inventory createInventory(Inventory inventory) {

        inventory.setDateAdded(LocalDate.now());

        if (inventory.getQuantity() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (inventory.getQuantity() <= 10) {
            inventory.setStatus(InventoryStatus.LOW_STOCK);
        } else {
            inventory.setStatus(InventoryStatus.AVAILABLE);
        }

        return inventoryRepository.save(inventory);
    }

    /**
     * Retrieves all inventory records across all retailers.
     *
     * @return List of all {@link Inventory} items.
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Finds a specific inventory record by ID.
     *
     * @param inventoryId The unique ID of the inventory.
     * @return The found {@link Inventory} record.
     * @throws InventoryNotFoundException if no record exists for the given ID.
     */
    public Inventory getInventoryById(@NonNull Long inventoryId) {

        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory not found with ID: " + inventoryId));
    }

    /**
     * Retrieves all inventory items belonging to a specific retailer.
     *
     * @param retailerId The unique ID of the retailer.
     * @return A list of {@link Inventory} records for that retailer.
     */
    public List<Inventory> getInventoryByRetailer(@NonNull Long retailerId) {

        return inventoryRepository.findByRetailerId(retailerId);
    }
}
