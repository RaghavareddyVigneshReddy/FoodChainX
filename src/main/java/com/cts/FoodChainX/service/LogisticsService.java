package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.model.*;
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
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) { 
        ProductionBatch batchObj = batchRepository.findById(request.getBatchId().longValue()) 
                .orElseThrow(() -> new RuntimeException("Batch not found")); 
 
        if (!"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) { 
            throw new IllegalArgumentException("Batch is not Compliant."); 
        } 
 
        User distributorObj = userRepository.findById(request.getDistributorId().longValue()) 
                .orElseThrow(() -> new RuntimeException("Distributor not found")); 
 
        Shipment shipment = new Shipment(); 
        shipment.setBatch(batchObj);  
        shipment.setDistributor(distributorObj); 
        shipment.setDepartureDate(request.getDepartureDate()); 
        shipment.setArrivalDate(request.getArrivalDate()); 
        shipment.setStatus("PENDING"); 
 
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment)); 
    } 

    @Transactional
    public ShipmentResponseDTO updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if ("DELIVERED".equalsIgnoreCase(request.getStatus())) {
            // Ensure you use the ID from the fetched batch object
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
        // 1. Fetch all required objects
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId().longValue()) 
                .orElseThrow(() -> new RuntimeException("Warehouse not found")); 
 
        if ("Full".equalsIgnoreCase(warehouse.getStatus())) { 
            throw new RuntimeException("409 Conflict: Warehouse is at maximum capacity"); 
        } 
 
        Shipment shipment = shipmentRepository.findById(request.getShipmentId().longValue()) 
                .orElseThrow(() -> new RuntimeException("Shipment not found")); 
                 
        User retailer = userRepository.findById(request.getRetailerId().longValue()) 
                .orElseThrow(() -> new RuntimeException("Retailer not found")); 
        
        ProductionBatch batch = shipment.getBatch();

        // 2. Save Delivery
        Delivery delivery = new Delivery(); 
        delivery.setShipment(shipment); 
        delivery.setRetailer(retailer); 
        delivery.setDate(request.getDeliveryDate());  
        delivery.setStatus("DELIVERED"); 
        deliveryRepository.save(delivery);

        // 3. Automation: Inventory
        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailer.getUserId());
        newInventory.setQuantity(batch.getQuantity().longValue());
        newInventory.setDateAdded(LocalDate.now());
        newInventory.setStatus("ACTIVE");
        inventoryRepository.save(newInventory);

        // 4. Side Effect: Traceability
        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailer);
        deliveryTrace.setStatus("ON_SHELF_AT_STORE");
        deliveryTrace.setDate(LocalDate.now());
        traceRecordRepository.save(deliveryTrace);
        
        log.info("Logistics flow completed for Batch {}", batch.getProductionId());
    }

    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) { 
        return ShipmentResponseDTO.builder() 
                .shipmentId(s.getShipmentId().longValue())  
                .batchId(s.getBatch() != null ? s.getBatch().getProductionId() : null) 
                .distributorId(s.getDistributor() != null ? s.getDistributor().getUserId().longValue() : null) 
                .status(s.getStatus()) 
                .departureDate(s.getDepartureDate()) 
                .arrivalDate(s.getArrivalDate()) 
                .build(); 
    } 
}