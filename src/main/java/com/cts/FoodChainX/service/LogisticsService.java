package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogisticsService {

    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private ProductionBatchRepository batchRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TraceRecordRepository traceRecordRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
   
    ProductionBatch batchObj = batchRepository.findById(request.getBatchId())
            .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!"PASSED".equalsIgnoreCase(batchObj.getQualityStatus()) && 
    !"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalArgumentException("Batch is not Compliant or has not passes inspection. Shipment cannot be initiated.");
        }

        Shipment shipment = new Shipment();
        // FIXED: Use setBatch and setDistributor to match your Model fields
        shipment.setBatch(request.getBatchId().intValue()); 
        shipment.setDistributor(request.getDistributorId().intValue());
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("IN_TRANSIT");

        TraceRecord shipmentTrace = new TraceRecord();
    shipmentTrace.setProductionBatch(batchObj);
    shipmentTrace.setFarm(batchObj.getFarm());
    shipmentTrace.setDistributor(userRepository.findById(request.getDistributorId()).get()); // distributorid
    shipmentTrace.setStatus("IN_TRANSIT");
    shipmentTrace.setDate(LocalDate.now());
    traceRecordRepository.save(shipmentTrace);

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    public ShipmentResponseDTO updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        // FIXED: Convert Long id to Integer for findById
        Shipment shipment = shipmentRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Shipment record not found"));

        shipment.setStatus(request.getStatus());
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(w -> WarehouseResponseDTO.builder()
                        .warehouseId(w.getWarehouseId().longValue())
                        .location(w.getLocation())
                        .capacity(w.getCapacity())
                        .status(w.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

 @Transactional
public void recordDelivery(DeliveryRequestDTO request) {
    // 1. Fetch Dependencies
    Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId().intValue())
            .orElseThrow(() -> new RuntimeException("Warehouse not found"));

    if ("Full".equalsIgnoreCase(warehouse.getStatus())) {
        throw new RuntimeException("409 Conflict: Warehouse is at maximum capacity");
    }

    Shipment shipment = shipmentRepository.findById(request.getShipmentId().intValue())
            .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + request.getShipmentId()));

    ProductionBatch batch = batchRepository.findById(shipment.getBatch().longValue())
            .orElseThrow(() -> new RuntimeException("Batch not found for this shipment"));

            // This updates the status from 'SHIPPED' to 'DELIVERED'
    shipment.setStatus("DELIVERED");
    shipmentRepository.save(shipment);
    // 2. Save the Delivery Record
    Delivery delivery = new Delivery();
    delivery.setShipmentId(request.getShipmentId().intValue());
    delivery.setRetailerId(request.getRetailerId().intValue()); 
    delivery.setDate(request.getDeliveryDate()); 
    delivery.setStatus("DELIVERED");
    deliveryRepository.save(delivery);

    // 3. AUTOMATION: Insert into INVENTORY
    // This makes the product available for sale to consumers
    Inventory newInventory = new Inventory();
    newInventory.setBatchId(batch.getProductionId().intValue());
    newInventory.setRetailerId(request.getRetailerId().intValue()); // Assuming RetailerID = WarehouseID
    newInventory.setQuantity(batch.getQuantity().intValue()); // Transfer full batch quantity to stock
    newInventory.setDateAdded(LocalDate.now());
    newInventory.setStatus("ACTIVE");
    inventoryRepository.save(newInventory);

    // 4. SIDE EFFECT: Update Traceability
    TraceRecord deliveryTrace = new TraceRecord();
    deliveryTrace.setProductionBatch(batch);
    deliveryTrace.setFarm(batch.getFarm());
    
    User distributor = userRepository.findById(shipment.getDistributor().longValue())
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

    // Helper Method to resolve convertToShipmentResponseDTO errors
    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) {
        return ShipmentResponseDTO.builder()
                .shipmentId(s.getShipmentId().longValue())
                .batchId(s.getBatch().longValue())
                .distributorId(s.getDistributor() != null ? s.getDistributor().longValue() : null)
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}