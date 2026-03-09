package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.model.Sale;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.InventoryRepository;
import com.cts.FoodChainX.repository.SaleRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import com.cts.FoodChainX.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TraceRecordRepository traceRecordRepository;

    @Transactional
    public Sale createSale(Sale sale) {
        Inventory inventory = getInventory(sale.getInventoryId());
        validateStock(inventory, sale.getQuantity());
        updateInventory(inventory, sale.getQuantity());
        // 1. Set sale metadata
        sale.setDate(LocalDate.now());
        Sale savedSale = saleRepository.save(sale);
        // 2. NEW: Update Traceability Record for the Batch
        updateTraceRecord(inventory.getBatchId(), sale.getConsumerId());

        return savedSale;
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

private void updateTraceRecord(Integer batchId, Integer consumerId) {
    // 1. Fetch the User entity (Consumer) first
    User consumer = userRepository.findById(consumerId.longValue())
            .orElseThrow(() -> new RuntimeException("Consumer not found"));

    // 2. Update the TraceRecord using the User object
    traceRecordRepository.findByProductionBatch_ProductionId(batchId.longValue())
        .ifPresent(record -> {
            record.setConsumer(consumer); // Correctly passing the User entity
            record.setStatus("SOLD");
            record.setDate(LocalDate.now());
            traceRecordRepository.save(record);
        });
}
}