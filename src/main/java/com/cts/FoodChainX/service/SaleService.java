package com.cts.foodchainx.service;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.model.Sale;
import com.cts.foodchainx.model.TraceRecord;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.InventoryRepository;
import com.cts.foodchainx.repository.SaleRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    @Transactional
    @Auditable(action = "CREATE_SALE", resource = "INVENTORY_SALE")
    public Sale createSale(@NonNull Sale sale) {
        // Fix: Use Objects.requireNonNull directly on the result of getInventory
        // This 'promises' the compiler that inventory is not null for the rest of the method
        Inventory inventory = Objects.requireNonNull(
            getInventory(Objects.requireNonNull(sale.getInventoryId(), "Inventory ID is required")),
            "Inventory record could not be retrieved"
        );

        Long quantity = Objects.requireNonNull(sale.getQuantity(), "Quantity is required for sale");

        // These calls will now be 100% green
        validateStock(inventory, quantity);
        updateInventory(inventory, quantity);

        sale.setDate(LocalDate.now());
        sale.setBatchId(inventory.getBatchId());

        Sale savedSale = saleRepository.save(sale);

        // Clear Ln 49-ish warnings by ensuring batch and consumer IDs are non-null
        updateTraceRecord(
            Objects.requireNonNull(inventory.getBatchId(), "Batch ID must not be null"),
            Objects.requireNonNull(sale.getConsumerId(), "Consumer ID must not be null")
        );

        return savedSale;
    }

    @NonNull // Adding this annotation helps the compiler trust the return value
    private Inventory getInventory(@NonNull Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found with ID: " + inventoryId));
    }

    private void validateStock(@NonNull Inventory inventory, @NonNull Long quantity) {
        if (inventory.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock available");
        }
    }

    private void updateInventory(@NonNull Inventory inventory, @NonNull Long quantity) {
        long remainingStock = inventory.getQuantity() - quantity;
        inventory.setQuantity(remainingStock);
        if (remainingStock == 0) {
            inventory.setStatus("OUT_OF_STOCK");
        }
        inventoryRepository.save(inventory);
    }

    private void updateTraceRecord(@NonNull Long batchId, @NonNull Long consumerId) {
        User consumer = userRepository.findById(consumerId)
                .orElseThrow(() -> new EntityNotFoundException("Consumer not found"));

        traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId)
            .stream()
            .findFirst() 
            .ifPresent(latestRecord -> {
                var saleRecord = new TraceRecord();
                saleRecord.setProductionBatch(latestRecord.getProductionBatch());
                saleRecord.setFarm(latestRecord.getFarm());
                saleRecord.setDistributor(latestRecord.getDistributor());
                saleRecord.setRetailer(latestRecord.getRetailer());
                
                saleRecord.setConsumer(consumer); 
                saleRecord.setStatus("SOLD");
                saleRecord.setDate(LocalDate.now());
                
                traceRecordRepository.save(saleRecord);
                log.info("New Trace Entry: Batch {} marked as SOLD to {}", batchId, consumer.getName());
            });
    }
}