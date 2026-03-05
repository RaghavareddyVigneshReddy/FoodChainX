package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retail/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getInventoryById(@PathVariable Integer id) {
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/retailer/{retailerId}")
    public List<Inventory> getInventoryByRetailer(@PathVariable Integer retailerId) {
        return inventoryService.getInventoryByRetailer(retailerId);
    }
}
