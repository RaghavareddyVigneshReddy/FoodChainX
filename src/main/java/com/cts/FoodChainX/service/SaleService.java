package com.cts.FoodChainX.service;

import com.cts.FoodChainX.aspect.Auditable;
import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.model.Sale;
import com.cts.FoodChainX.model.TraceRecord;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.InventoryRepository;
import com.cts.FoodChainX.repository.SaleRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import com.cts.FoodChainX.repository.UserRepository;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
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
    @Auditable(action = "CREATE_SALE", resource = "INVENTORY_SALE") // ADD THIS
    public Sale createSale(Sale sale) {
        Inventory inventory = getInventory(sale.getInventoryId());
        validateStock(inventory, sale.getQuantity());
        updateInventory(inventory, sale.getQuantity());
        // Set sale metadata
        sale.setDate(LocalDate.now());
        Sale savedSale = saleRepository.save(sale);
        // Update Traceability Record for the Batch
        updateTraceRecord(inventory.getBatchId(), sale.getConsumerId());

        return savedSale;
    }

    private Inventory getInventory(@Nonnull Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    private void validateStock(Inventory inventory, Long quantity) {
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }
    }

    private void updateInventory(Inventory inventory, Long quantity) {
        Long remainingStock = inventory.getQuantity() - quantity;
        inventory.setQuantity(remainingStock);
        if (remainingStock == 0) {
            inventory.setStatus("OUT_OF_STOCK");
        }
        inventoryRepository.save(inventory);
    }

    private void updateTraceRecord(Long batchId, Long consumerId) {
        // 1. Fetch the Consumer
        User consumer = userRepository.findById(consumerId.longValue())
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        // 2. Fetch the latest record to copy the Batch and Farm details
        traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId.longValue())
            .stream()
            .findFirst() 
            .ifPresent(latestRecord -> {
                // 3. Create a NEW TraceRecord for the sale event
                var saleRecord = new TraceRecord();
                saleRecord.setProductionBatch(latestRecord.getProductionBatch());
                saleRecord.setFarm(latestRecord.getFarm());
                saleRecord.setDistributor(latestRecord.getDistributor());
                saleRecord.setRetailer(latestRecord.getRetailer()); // Keep the retailer info
                
                // 4. Set the Sale Specifics
                saleRecord.setConsumer(consumer); 
                saleRecord.setStatus("SOLD");
                saleRecord.setDate(LocalDate.now());
                
                traceRecordRepository.save(saleRecord);
                
                log.info("New Trace Entry: Batch {} marked as SOLD to {}", batchId, consumer.getName());
            });
    }
}