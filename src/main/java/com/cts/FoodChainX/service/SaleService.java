package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.model.Sale;
import com.cts.FoodChainX.repository.InventoryRepository;
import com.cts.FoodChainX.repository.SaleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public Sale createSale(Sale sale) {

        Inventory inventory = getInventory(sale.getInventoryId());

        validateStock(inventory, sale.getQuantity());

        updateInventory(inventory, sale.getQuantity());

        sale.setDate(LocalDate.now());

        return saleRepository.save(sale);
    }

    private Inventory getInventory(Integer inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    private void validateStock(Inventory inventory, Integer quantity) {
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }
    }

    private void updateInventory(Inventory inventory, Integer quantity) {

        Integer remainingStock = inventory.getQuantity() - quantity;

        inventory.setQuantity(remainingStock);

        if (remainingStock == 0) {
            inventory.setStatus("OUT_OF_STOCK");
        }

        inventoryRepository.save(inventory);
    }
}