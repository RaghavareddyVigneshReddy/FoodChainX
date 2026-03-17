package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
import com.cts.foodchainx.service.LogisticsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link LogisticsService} handling business rules for the food supply chain.
 * This service manages the physical transition of goods between participants (Farms, Distributors, Retailers)
 * while ensuring warehouse capacity constraints and real-time inventory synchronization.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final ShipmentRepository shipmentRepository;
    private final ProductionBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryRepository deliveryRepository;
    private final TraceRecordRepository traceRecordRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Registers a new warehouse facility for a distributor.
     * Initializes the facility with zero stock and an 'AVAILABLE' status.
     *
     * @param request the warehouse details including location, capacity, and distributor ID
     * @return the persisted {@link WarehouseResponseDTO}
     * @throws EntityNotFoundException if the associated distributor user is not found
     */
    @Override
    @Transactional
    public WarehouseResponseDTO registerWarehouse(@NonNull WarehouseRequestDTO request) {
        User distributorObj = userRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));

        Warehouse warehouse = new Warehouse();
        warehouse.setDistributor(distributorObj);
        warehouse.setLocation(request.getLocation());
        warehouse.setCapacity(request.getCapacity());
        
        // Use 0.0 to match the Double type in the entity
        warehouse.setCurrentStockLevel(0.0); 
        warehouse.setStatus(WarehouseStatus.AVAILABLE);

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("New warehouse registered at {} for distributor {}", saved.getLocation(), distributorObj.getUserId());
        return convertToWarehouseResponseDTO(saved);
    }

    /**
     * Initiates the shipment of a production batch from a farm to a distributor.
     *
     * @param request details containing the batch ID, distributor ID, and schedule
     * @return the created {@link ShipmentResponseDTO}
     */
    @Override
    @Transactional
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS")
    public ShipmentResponseDTO initiateShipment(@NonNull ShipmentRequestDTO request) {
        ProductionBatch batchObj = batchRepository.findById(Objects.requireNonNull(request.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
        
        User distributorObj = userRepository.findById(Objects.requireNonNull(request.getDistributorId()))
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));

        if (batchObj.getQualityStatus() != QualityStatus.PASSED) {
            log.warn("Shipment initiation blocked: Batch {} has failed quality status", batchObj.getProductionId());
            throw new EntityNotFoundException("Batch is not Compliant. Shipment cannot be initiated.");
        }

        Shipment shipment = new Shipment();
        shipment.setBatch(batchObj);
        shipment.setDistributor(distributorObj);
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        TraceRecord shipmentTrace = new TraceRecord();
        shipmentTrace.setProductionBatch(batchObj);
        shipmentTrace.setFarm(batchObj.getFarm());
        shipmentTrace.setDistributor(distributorObj); 
        shipmentTrace.setStatus(TraceStatus.IN_TRANSIT);
        shipmentTrace.setDate(LocalDate.now());
        traceRecordRepository.save(shipmentTrace);

        log.info("Shipment initiated for Batch: {} by Distributor: {}", batchObj.getProductionId(), distributorObj.getUserId());
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    /**
     * Updates shipment status and handles warehouse receiving logic.
     *
     * @param id the unique shipment ID
     * @param request the status update payload
     * @return the updated {@link ShipmentResponseDTO}
     * @throws WarehouseCapacityException if receiving the shipment would exceed warehouse limits
     */
    @Override
    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS")
    public ShipmentResponseDTO updateShipmentStatus(@NonNull Long id, @NonNull ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            Warehouse warehouse = warehouseRepository.findByDistributor_UserId(shipment.getDistributor().getUserId())
                    .stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No warehouse found for the distributor"));

            double incomingQty = shipment.getBatch().getQuantity();
            double updatedStock = warehouse.getCurrentStockLevel() + incomingQty;

            if (updatedStock > (double) warehouse.getCapacity()) {
                throw new WarehouseCapacityException("Cannot receive shipment: Warehouse capacity exceeded");
            }

            warehouse.setCurrentStockLevel(updatedStock);
            if (updatedStock == (double) warehouse.getCapacity()) {
                warehouse.setStatus(WarehouseStatus.FULL);
            }
            warehouseRepository.save(warehouse);

            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatch().getProductionId())
                .stream()
                .findFirst()
                .ifPresent(traceRecord -> {
                    traceRecord.setStatus(TraceStatus.ARRIVED_AT_WAREHOUSE);
                    traceRecordRepository.save(traceRecord);
                });
            
            log.info("Shipment {} received. Warehouse {} stock increased to {}", id, warehouse.getWarehouseId(), updatedStock);
        }
        
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    /**
     * Finalizes the delivery of a batch from the warehouse to a retail store.
     *
     * @param request payload containing warehouse, shipment, and retailer identifiers
     */
    @Override
    @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS")
    public void recordDelivery(@NonNull DeliveryRequestDTO request) {
        Warehouse warehouse = warehouseRepository.findById(Objects.requireNonNull(request.getWarehouseId()))
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        Shipment shipment = shipmentRepository.findById(Objects.requireNonNull(request.getShipmentId()))
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        User retailerObj = userRepository.findById(Objects.requireNonNull(request.getRetailerId()))
                .orElseThrow(() -> new EntityNotFoundException("Retailer user not found"));

        ProductionBatch batch = shipment.getBatch();

        // Stock reduction logic with explicit double handling
        double departingQty = batch.getQuantity();
        double updatedStock = Math.max(0.0, warehouse.getCurrentStockLevel() - departingQty);
        warehouse.setCurrentStockLevel(updatedStock);

        if (updatedStock < (double) warehouse.getCapacity()) {
            warehouse.setStatus(WarehouseStatus.AVAILABLE);
        }
        warehouseRepository.save(warehouse);

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        // Create Delivery Record
        Delivery delivery = new Delivery();
        delivery.setShipment(shipment);
        delivery.setRetailer(retailerObj);
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus(ShipmentStatus.DELIVERED);
        deliveryRepository.save(delivery);

        // Retailer Inventory Initialization - Explicit cast from double to long
        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailerObj.getUserId()); 
        long quantity = batch.getQuantity().longValue(); // Use longValue() helper
        newInventory.setQuantity(quantity); 
        newInventory.setDateAdded(LocalDate.now());

        if (quantity == 0) {
            newInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (quantity <= 10) {
            newInventory.setStatus(InventoryStatus.LOW_STOCK);
        } else {
            newInventory.setStatus(InventoryStatus.AVAILABLE);
        }
        inventoryRepository.save(newInventory);

        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailerObj);
        deliveryTrace.setStatus(TraceStatus.ON_SHELF_AT_STORE);
        deliveryTrace.setDate(LocalDate.now());
        traceRecordRepository.save(deliveryTrace);
        
        log.info("Delivery recorded. Warehouse {} stock decreased to {}. Batch {} moved to Retailer {}", 
                warehouse.getWarehouseId(), updatedStock, batch.getProductionId(), retailerObj.getUserId());
    }

    @Override
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToWarehouseResponseDTO)
                .toList();
    }

    private WarehouseResponseDTO convertToWarehouseResponseDTO(Warehouse w) {
        return WarehouseResponseDTO.builder()
                .warehouseId(w.getWarehouseId())
                .location(w.getLocation())
                .capacity(w.getCapacity())
                .status(w.getStatus())
                .build();
    }

    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) {
        return ShipmentResponseDTO.builder()
                .shipmentId(s.getShipmentId())
                .batchId(s.getBatch().getProductionId())
                .distributorId(s.getDistributor().getUserId())
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}