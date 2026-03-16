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
 * Implementation of LogisticsService handling business rules for the supply chain.
 * Manages the transition of goods between farms, distributors, and retailers.
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

        // Logging the physical movement in TraceRecord
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

    @Override
    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS")
    public ShipmentResponseDTO updateShipmentStatus(@NonNull Long id, @NonNull ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatch().getProductionId())
                .stream()
                .findFirst()
                .ifPresent(traceRecord -> {
                    traceRecord.setStatus(TraceStatus.ARRIVED_AT_WAREHOUSE);
                    traceRecordRepository.save(traceRecord);
                });
            log.info("Shipment {} marked as DELIVERED", id);
        }
        
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS")
    public void recordDelivery(@NonNull DeliveryRequestDTO request) {
        Warehouse warehouse = warehouseRepository.findById(Objects.requireNonNull(request.getWarehouseId()))
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        if (warehouse.getStatus() == WarehouseStatus.FULL) {
           log.error("Delivery failed: Warehouse {} is full", warehouse.getWarehouseId());
           throw new WarehouseCapacityException("Warehouse " + warehouse.getWarehouseId() + " is at maximum capacity");
        }

        Shipment shipment = shipmentRepository.findById(Objects.requireNonNull(request.getShipmentId()))
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        User retailerObj = userRepository.findById(Objects.requireNonNull(request.getRetailerId()))
                .orElseThrow(() -> new EntityNotFoundException("Retailer user not found"));

        ProductionBatch batch = shipment.getBatch();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        // Create Delivery Record
        Delivery delivery = new Delivery();
        delivery.setShipment(shipment);
        delivery.setRetailer(retailerObj);
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus(ShipmentStatus.DELIVERED);
        deliveryRepository.save(delivery);

        // Update Retailer Inventory
        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailerObj.getUserId()); 
        long quantity = batch.getQuantity().longValue();
        newInventory.setQuantity(quantity); 
        newInventory.setDateAdded(LocalDate.now());

        // Dynamic stock status mapping
        if (quantity == 0) {
            newInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (quantity <= 10) {
            newInventory.setStatus(InventoryStatus.LOW_STOCK);
        } else {
            newInventory.setStatus(InventoryStatus.AVAILABLE);
        }
        
        inventoryRepository.save(newInventory);

        // Final Traceability link
        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailerObj);
        deliveryTrace.setStatus(TraceStatus.ON_SHELF_AT_STORE);
        deliveryTrace.setDate(LocalDate.now());
        traceRecordRepository.save(deliveryTrace);
        
        log.info("Delivery recorded. Batch {} is now inventory for Retailer {}", batch.getProductionId(), retailerObj.getUserId());
    }

    @Override
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(w -> WarehouseResponseDTO.builder()
                        .warehouseId(w.getWarehouseId())
                        .location(w.getLocation())
                        .capacity(w.getCapacity())
                        .status(w.getStatus())
                        .build())
                .toList();
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