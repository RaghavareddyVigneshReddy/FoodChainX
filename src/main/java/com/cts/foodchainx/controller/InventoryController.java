package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.inventory.InventoryRequestDTO;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.serviceimpl.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST Controller for managing retail inventory operations.
 * <p>
 * Provides endpoints to create, retrieve, and filter inventory records
 * associated with specific retailers and batches within the food chain system.
 * </p>
 *
 * @author FoodChainX Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/retail/inventory")
@RequiredArgsConstructor
public class InventoryController {

    /**
     * Service layer dependency for inventory logic.
     */
    private final InventoryServiceImpl inventoryServiceImpl;

    /**
     * Creates a new inventory record.
     * * @param dto The data transfer object containing retailer ID, batch ID, and quantity.
     * @return The persisted {@link Inventory} entity.
     * @throws NullPointerException if any required field in the DTO is null.
     */
    @PostMapping
    public Inventory createInventory(@RequestBody @NonNull InventoryRequestDTO dto) {
        Inventory inventory = new Inventory();

        // Mapping logic with strict null-safety validation
        inventory.setRetailerId(Objects.requireNonNull(dto.getRetailerId(), "Retailer ID must not be null"));
        inventory.setBatchId(Objects.requireNonNull(dto.getBatchId(), "Batch ID must not be null"));
        inventory.setQuantity(Objects.requireNonNull(dto.getQuantity(), "Quantity must not be null"));

        return inventoryServiceImpl.createInventory(inventory);
    }

    /**
     * Retrieves a list of all inventory records currently in the system.
     *
     * @return A list of {@link Inventory} objects.
     */
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryServiceImpl.getAllInventory();
    }

    /**
     * Retrieves a specific inventory record by its unique database identifier.
     *
     * @param id The unique ID of the inventory record.
     * @return The matching {@link Inventory} record.
     */
    @GetMapping("/{id}")
    public Inventory getInventoryById(@PathVariable @NonNull Long id) {
        return inventoryServiceImpl.getInventoryById(id);
    }

    /**
     * Retrieves all inventory records associated with a specific retailer.
     *
     * @param retailerId The ID of the retailer to filter by.
     * @return A list of {@link Inventory} records belonging to the specified retailer.
     */
    @GetMapping("/retailer/{retailerId}")
    public List<Inventory> getInventoryByRetailer(@PathVariable @NonNull Long retailerId) {
        return inventoryServiceImpl.getInventoryByRetailer(retailerId);
    }
}