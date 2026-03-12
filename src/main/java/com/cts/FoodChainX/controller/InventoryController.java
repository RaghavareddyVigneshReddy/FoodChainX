package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.inventory.InventoryRequestDTO;
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
    public Inventory createInventory(@RequestBody InventoryRequestDTO dto) {
        Inventory inventory=new Inventory();

        inventory.setRetailerId(dto.getRetailerId());
        inventory.setBatchId(dto.getBatchId());
        inventory.setQuantity(dto.getQuantity());

        return inventoryService.createInventory(inventory);
    }

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/retailer/{retailerId}")
    public List<Inventory> getInventoryByRetailer(@PathVariable Long retailerId) {
        return inventoryService.getInventoryByRetailer(retailerId);
    }
}
