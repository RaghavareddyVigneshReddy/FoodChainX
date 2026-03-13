package com.cts.foodchainx.service;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Auditable(action = "ADD_RETAIL_INVENTORY", resource = "INVENTORY")
    public Inventory createInventory(Inventory inventory) {
        inventory.setDateAdded(LocalDate.now());

        if (inventory.getQuantity() == 0) {
            inventory.setStatus("OUT_OF_STOCK");
        } else if (inventory.getQuantity() <= 10) {
            inventory.setStatus("LOW_STOCK");
        } else {
            inventory.setStatus("AVAILABLE");
        }

        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(@NonNull Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with ID: " + inventoryId));
    }

    public List<Inventory> getInventoryByRetailer(@NonNull Long retailerId) {
        return inventoryRepository.findByRetailerId(retailerId);
    }
}