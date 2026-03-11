package com.cts.FoodChainX.service;

import com.cts.FoodChainX.aspect.Auditable;
import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.repository.InventoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Auditable(action = "ADD_RETAIL_INVENTORY", resource = "INVENTORY")
    public Inventory createInventory(Inventory inventory) {

        // set system generated date
        inventory.setDateAdded(LocalDate.now());

        // determine status automatically
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

    public Inventory getInventoryById(Long inventoryId) {

        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isPresent()) {
            return inventory.get();
        } else {
            throw new RuntimeException("Inventory not found");
        }
    }

    public List<Inventory> getInventoryByRetailer(Long retailerId) {
        return inventoryRepository.findByRetailerId(retailerId);
    }
}