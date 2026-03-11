package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.repository.InventoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public Inventory createInventory(Inventory inventory) {

        if (inventory.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        inventory.setStatus("ACTIVE");

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