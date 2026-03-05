package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.model.*;
import com.cts.FoodChainX.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
   
    ProductionBatch batchObj = batchRepository.findById(request.getBatchId())
            .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
            throw new IllegalArgumentException("Batch is not Compliant.");
        }

        Shipment shipment = new Shipment();
        // FIXED: Use setBatch and setDistributor to match your Model fields
        shipment.setBatch(request.getBatchId().intValue()); 
        shipment.setDistributor(request.getDistributorId().intValue());
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus("PENDING");

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
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId().intValue())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if ("Full".equalsIgnoreCase(warehouse.getStatus())) {
            throw new RuntimeException("409 Conflict: Warehouse is at maximum capacity");
        }

        Delivery delivery = new Delivery();
        // FIXED: Use correct request getter names
        delivery.setShipmentId(request.getShipmentId().intValue());
        delivery.setRetailerId(request.getWarehouseId().intValue()); 
        delivery.setDate(request.getDeliveryDate()); 
        delivery.setStatus("DELIVERED");
        
        deliveryRepository.save(delivery);
    }

    // Helper Method to resolve convertToShipmentResponseDTO errors
    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) {
        return ShipmentResponseDTO.builder()
                .shipmentId(s.getShipmentId().longValue())
                .batchId(s.getBatch().longValue()) // Match your private Integer batch
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}