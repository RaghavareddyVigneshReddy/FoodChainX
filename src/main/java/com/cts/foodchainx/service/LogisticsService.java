package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsService {

    // SonarQube S1192: Define a constant instead of duplicating this literal "DELIVERED"
    private static final String STATUS_DELIVERED = "DELIVERED";

    private final ShipmentRepository shipmentRepository;
    private final ProductionBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryRepository deliveryRepository;
    private final TraceRecordRepository traceRecordRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS")
    public ShipmentResponseDTO initiateShipment(@NonNull ShipmentRequestDTO request) {
        // Fix null safety: Ensure ID is not null before repository call
        ProductionBatch batchObj = batchRepository.findById(Objects.requireNonNull(request.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
        
        User distributorObj = userRepository.findById(Objects.requireNonNull(request.getDistributorId()))
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));

        if (!"PASSED".equalsIgnoreCase(batchObj.getQualityStatus()) &&
            !"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalStateException("Batch is not Compliant. Shipment cannot be initiated.");
        }

        Shipment shipment = new Shipment();
        shipment.setBatch(batchObj);
        shipment.setDistributor(distributorObj);
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("IN_TRANSIT");

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
    public ShipmentResponseDTO updateShipmentStatus(@NonNull Long id, @NonNull ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if (STATUS_DELIVERED.equalsIgnoreCase(request.getStatus())) {
            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatch().getProductionId())
                .stream()
                .findFirst()
                .ifPresent(traceRecord -> {
                    traceRecord.setStatus("ARRIVED_AT_WAREHOUSE");
                    traceRecordRepository.save(traceRecord);
                });
        }
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS")
    public void recordDelivery(@NonNull DeliveryRequestDTO request) {
        Warehouse warehouse = warehouseRepository.findById(Objects.requireNonNull(request.getWarehouseId()))
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        if ("Full".equalsIgnoreCase(warehouse.getStatus())) {
            throw new IllegalStateException("Warehouse is at maximum capacity");
        }

        Shipment shipment = shipmentRepository.findById(Objects.requireNonNull(request.getShipmentId()))
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        User retailerObj = userRepository.findById(Objects.requireNonNull(request.getRetailerId()))
                .orElseThrow(() -> new EntityNotFoundException("Retailer user not found"));

        ProductionBatch batch = shipment.getBatch();
        shipment.setStatus(STATUS_DELIVERED); // Used constant here
        shipmentRepository.save(shipment);

        Delivery delivery = new Delivery();
        delivery.setShipment(shipment);
        delivery.setRetailer(retailerObj);
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus(STATUS_DELIVERED); // Used constant here
        deliveryRepository.save(delivery);

        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailerObj.getUserId()); 
        newInventory.setQuantity(batch.getQuantity().longValue()); 
        newInventory.setDateAdded(LocalDate.now());
        newInventory.setStatus("ACTIVE");
        inventoryRepository.save(newInventory);

        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailerObj);
        deliveryTrace.setStatus("ON_SHELF_AT_STORE");
        deliveryTrace.setDate(LocalDate.now());
        traceRecordRepository.save(deliveryTrace);
    }

    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(w -> WarehouseResponseDTO.builder()
                        .warehouseId(w.getWarehouseId())
                        .location(w.getLocation())
                        .capacity(w.getCapacity())
                        .status(w.getStatus())
                        .build())
                .toList(); // SonarQube S6204: Changed to .toList()
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