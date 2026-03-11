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
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS") // Added Annotation
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
        ProductionBatch batchObj = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Validation logic from File 1
        if (!"PASSED".equalsIgnoreCase(batchObj.getQualityStatus()) &&
            !"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalArgumentException("Batch is not Compliant or has not passed inspection. Shipment cannot be initiated.");
        }

        Shipment shipment = new Shipment();
        shipment.setBatchId(request.getBatchId());
        shipment.setDistributorId(request.getDistributorId());
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("IN_TRANSIT");

        // Advanced Traceability logic from File 1
        TraceRecord shipmentTrace = new TraceRecord();
        shipmentTrace.setProductionBatch(batchObj);
        shipmentTrace.setFarm(batchObj.getFarm());
        shipmentTrace.setDistributor(userRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new RuntimeException("Distributor not found"))); 
        shipmentTrace.setStatus("IN_TRANSIT");
        shipmentTrace.setDate(LocalDate.now());
        traceRecordRepository.save(shipmentTrace);

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS") // Added Annotation
    public ShipmentResponseDTO updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if ("DELIVERED".equalsIgnoreCase(request.getStatus())) {
            // Using the specific repository method from File 1
            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatchId())
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
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS") // Added Annotation
    public void recordDelivery(DeliveryRequestDTO request) {
        // 1. Fetch Dependencies
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if ("Full".equalsIgnoreCase(warehouse.getStatus())) {
            throw new RuntimeException("409 Conflict: Warehouse is at maximum capacity");
        }

        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + request.getShipmentId()));

        ProductionBatch batch = batchRepository.findById(shipment.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found for this shipment"));

        shipment.setStatus("DELIVERED");
        shipmentRepository.save(shipment);

        // 2. Save the Delivery Record
        Delivery delivery = new Delivery();
        delivery.setShipmentId(request.getShipmentId());
        delivery.setRetailerId(request.getRetailerId());
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus("DELIVERED");
        deliveryRepository.save(delivery);

        // 3. AUTOMATION: Insert into INVENTORY (Critical logic from File 1)
        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(request.getRetailerId()); 
        newInventory.setQuantity(batch.getQuantity().longValue()); 
        newInventory.setDateAdded(LocalDate.now());
        newInventory.setStatus("ACTIVE");
        inventoryRepository.save(newInventory);

        // 4. SIDE EFFECT: Update Traceability
        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
       
        User distributor = userRepository.findById(shipment.getDistributorId().longValue())
                .orElseThrow(() -> new RuntimeException("Distributor not found"));
        deliveryTrace.setDistributor(distributor);

        User retailer = userRepository.findById(request.getRetailerId().longValue())
                .orElseThrow(() -> new RuntimeException("Retailer user not found"));
        deliveryTrace.setRetailer(retailer);
       
        deliveryTrace.setStatus("ON_SHELF_AT_STORE");
        deliveryTrace.setDate(LocalDate.now());
       
        traceRecordRepository.save(deliveryTrace);
       
        log.info("Shipment, Delivery, Inventory, and Traceability all updated for Batch {}", batch.getProductionId());
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
                .batchId(s.getBatchId())
                .distributorId(s.getDistributorId())
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}