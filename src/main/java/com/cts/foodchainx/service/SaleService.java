package com.cts.foodchainx.service;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Service responsible for processing retail sales and maintaining the food supply chain traceability.
 * <p>
 * This service performs atomic transactions that decrease inventory stock and
 * create a new "SOLD" trace record to link the production batch to the final consumer.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final TraceRecordRepository traceRecordRepository;

    /**
     * Executes a complete sale transaction.
     * <p>
     * The process involves:
     * 1. Validating and retrieving inventory.
     * 2. Checking for sufficient stock levels.
     * 3. Deducting quantity and updating inventory status.
     * 4. Creating a new entry in the traceability history (TraceRecord).
     * </p>
     *
     * @param sale The sale request containing inventory, consumer, and quantity data.
     * @return The successfully persisted {@link Sale} record.
     * @throws EntityNotFoundException if Inventory or Consumer IDs do not exist.
     * @throws IllegalStateException if the requested quantity exceeds available stock.
     */
    @Transactional
    @Auditable(action = "CREATE_SALE", resource = "INVENTORY_SALE")
    public Sale createSale(@NonNull Sale sale) {
        Inventory inventory = Objects.requireNonNull(
                getInventory(Objects.requireNonNull(sale.getInventoryId(), "Inventory ID is required")),
                "Inventory record could not be retrieved"
        );

        Long quantity = Objects.requireNonNull(sale.getQuantity(), "Quantity is required for sale");

        validateStock(inventory, quantity);
        updateInventory(inventory, quantity);

        sale.setDate(LocalDate.now());
        sale.setBatchId(inventory.getBatchId());

        Sale savedSale = saleRepository.save(sale);

        updateTraceRecord(
                Objects.requireNonNull(inventory.getBatchId(), "Batch ID must not be null"),
                Objects.requireNonNull(sale.getConsumerId(), "Consumer ID must not be null")
        );

        return savedSale;
    }

    /**
     * Internal helper to fetch inventory with error handling.
     */
    @NonNull
    private Inventory getInventory(@NonNull Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with ID: " + inventoryId));
    }

    /**
     * Verifies if the inventory has enough stock for the requested sale.
     */
    private void validateStock(@NonNull Inventory inventory, @NonNull Long quantity) {
        if (inventory.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock available");
        }
    }

    /**
     * Deducts stock and updates the inventory status if it hits zero.
     */
    private void updateInventory(@NonNull Inventory inventory, @NonNull Long quantity) {
        long remainingStock = inventory.getQuantity() - quantity;
        inventory.setQuantity(remainingStock);
        if (remainingStock == 0) {
            inventory.setStatus("OUT_OF_STOCK");
        }
        inventoryRepository.save(inventory);
    }

    /**
     * Links the sale to the supply chain history.
     * Creates a new TraceRecord by copying data from the previous chain link (Retailer)
     * and adding the Consumer information.
     *
     * @param batchId The production batch associated with the sale.
     * @param consumerId The consumer purchasing the item.
     */
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