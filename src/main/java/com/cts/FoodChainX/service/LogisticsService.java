package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.aspect.Auditable; // Added Import
import com.cts.FoodChainX.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsService {

    private final ShipmentRepository shipmentRepository;
    private final ProductionBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryRepository deliveryRepository;
    private final TraceRecordRepository traceRecordRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS")
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
        // 1. Fetch the actual objects required by the Model
        ProductionBatch batchObj = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        
        User distributorObj = userRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new RuntimeException("Distributor not found"));

        // 2. Validation
        if (!"PASSED".equalsIgnoreCase(batchObj.getQualityStatus()) &&
            !"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalArgumentException("Batch is not Compliant. Shipment cannot be initiated.");
        }

        // 3. Map to Model using Objects instead of IDs
        Shipment shipment = new Shipment();
        shipment.setBatch(batchObj); // Changed from setBatchId
        shipment.setDistributor(distributorObj); // Changed from setDistributorId
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("IN_TRANSIT");

        // 4. Traceability logic
        TraceRecord shipmentTrace = new TraceRecord();
        shipmentTrace.setProductionBatch(batchObj);
        shipmentTrace.setFarm(batchObj.getFarm());
        shipmentTrace.setDistributor(distributorObj); 
        shipmentTrace.setStatus("IN_TRANSIT");
        shipmentTrace.setDate(LocalDate.now());
        traceRecordRepository.save(shipmentTrace);

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS")
    public ShipmentResponseDTO updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if ("DELIVERED".equalsIgnoreCase(request.getStatus())) {
            // Updated to use the batch object's ID for the repository query
            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatch().getProductionId())
                .stream()
                .findFirst()
                .ifPresent(record -> {
                    record.setStatus("ARRIVED_AT_WAREHOUSE");
                    traceRecordRepository.save(record);
                });
        }
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

 @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS")
    public void recordDelivery(DeliveryRequestDTO request) {
        // 1. Fetch Dependencies
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if ("Full".equalsIgnoreCase(warehouse.getStatus())) {
            throw new RuntimeException("409 Conflict: Warehouse is at maximum capacity");
        }

        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        User retailerObj = userRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer user not found"));

        // Access batch via relationship in Shipment
        ProductionBatch batch = shipment.getBatch();

        // 2. Update Shipment Status
        shipment.setStatus("DELIVERED");
        shipmentRepository.save(shipment);

        // 3. Save Delivery Record using Object Mappings
        Delivery delivery = new Delivery();
        delivery.setShipment(shipment);   // Changed from setShipmentId
        delivery.setRetailer(retailerObj); // Changed from setRetailerId
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus("DELIVERED");
        deliveryRepository.save(delivery);

        // 4. Automation: Insert into Inventory
        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailerObj.getUserId()); 
        newInventory.setQuantity(batch.getQuantity().longValue()); 
        newInventory.setDateAdded(LocalDate.now());
        newInventory.setStatus("ACTIVE");
        inventoryRepository.save(newInventory);

        // 5. Update Traceability
        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailerObj);
       
        deliveryTrace.setStatus("ON_SHELF_AT_STORE");
        deliveryTrace.setDate(LocalDate.now());
       
        traceRecordRepository.save(deliveryTrace);
        
        log.info("Delivery recorded for shipment {} to retailer {}", shipment.getShipmentId(), retailerObj.getName());
    }

    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(w -> WarehouseResponseDTO.builder()
                        .warehouseId(w.getWarehouseId())
                        .location(w.getLocation())
                        .capacity(w.getCapacity())
                        .status(w.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) {
        return ShipmentResponseDTO.builder()
                .shipmentId(s.getShipmentId())
                .batchId(s.getBatch().getProductionId()) // Map object back to ID for DTO
                .distributorId(s.getDistributor().getUserId()) // Map object back to ID for DTO
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}