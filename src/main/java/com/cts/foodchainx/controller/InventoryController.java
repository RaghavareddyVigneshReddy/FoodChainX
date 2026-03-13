package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.inventory.InventoryRequestDTO;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/retail/inventory")
@RequiredArgsConstructor // Fix S6813: Enables Constructor Injection via Lombok
public class InventoryController {

    // Final field ensures it's initialized via the constructor
    private final InventoryService inventoryService;

    @PostMapping
    public Inventory createInventory(@RequestBody @NonNull InventoryRequestDTO dto) {
        Inventory inventory = new Inventory();

        // Safe mapping with null checks to satisfy strict type safety
        inventory.setRetailerId(Objects.requireNonNull(dto.getRetailerId()));
        inventory.setBatchId(Objects.requireNonNull(dto.getBatchId()));
        inventory.setQuantity(Objects.requireNonNull(dto.getQuantity()));

        return inventoryService.createInventory(inventory);
    }

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getInventoryById(@PathVariable @NonNull Long id) {
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/retailer/{retailerId}")
    public List<Inventory> getInventoryByRetailer(@PathVariable @NonNull Long retailerId) {
        return inventoryService.getInventoryByRetailer(retailerId);
    }
}