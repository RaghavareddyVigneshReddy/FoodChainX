package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.aspect.Auditable;
import com.cts.FoodChainX.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS") // ADD THIS
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
        ProductionBatch batchObj = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!"Compliant".equalsIgnoreCase(batchObj.getQualityStatus()) && 
            !"APPROVED".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalArgumentException("Batch is not cleared for shipment.");
        }

        Shipment shipment = new Shipment();
        shipment.setBatchId(request.getBatchId()); 
        shipment.setDistributorId(request.getDistributorId());
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("IN_TRANSIT");

        traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(request.getBatchId())
            .stream()
            .findFirst()
            .ifPresent(record -> {
                record.setStatus("SHIPPED");
                traceRecordRepository.save(record);
            });

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS") // ADD THIS
    public ShipmentResponseDTO updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if ("DELIVERED".equalsIgnoreCase(request.getStatus())) {
            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDesc(shipment.getBatchId())
                .stream()
                .findFirst()
                .ifPresent(record -> {
                    record.setStatus("ARRIVED_AT_WAREHOUSE");
                    traceRecordRepository.save(record);
                });
        }

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    /**
     * ADDED: This method resolves the error in your LogisticsController
     */
    @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS") // ADD THIS
    public void recordDelivery(DeliveryRequestDTO request) {
        log.info("Recording delivery for shipment: {}", request.getShipmentId());
        
        // 1. Create and save the Delivery entity
        Delivery delivery = new Delivery();
        delivery.setShipmentId(request.getShipmentId());
        // Map warehouseId to retailer/destination ID
        delivery.setRetailerId(request.getWarehouseId()); 
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus("COMPLETED");
        
        deliveryRepository.save(delivery);

        // 2. Optional: Update the warehouse capacity status if needed
        warehouseRepository.findById(request.getWarehouseId())
            .ifPresent(w -> {
                if ("Full".equalsIgnoreCase(w.getStatus())) {
                    throw new RuntimeException("409 Conflict: Warehouse is already full");
                }
            });
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