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
    @Autowired
    private UserRepository userRepository;

    
    public ShipmentResponseDTO initiateShipment(ShipmentRequestDTO request) {
    // 1. Fetch the Batch object
    ProductionBatch batchObj = batchRepository.findById(request.getBatchId().longValue())
            .orElseThrow(() -> new RuntimeException("Batch not found"));

    if (!"Compliant".equalsIgnoreCase(batchObj.getQualityStatus())) {
        throw new IllegalArgumentException("Batch is not Compliant.");
    }

    // 2. Fetch the Distributor (User) object
    // Assuming you have a userRepository, otherwise use a reference
    User distributorObj = userRepository.findById(request.getDistributorId().longValue())
            .orElseThrow(() -> new RuntimeException("Distributor not found"));

    Shipment shipment = new Shipment();
    // FIXED: Passing objects instead of Integers to match JPA mappings
    shipment.setBatch(batchObj); 
    shipment.setDistributor(distributorObj);
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

    // Fetch the required objects for mapping
    Shipment shipmentObj = shipmentRepository.findById(request.getShipmentId().intValue())
            .orElseThrow(() -> new RuntimeException("Shipment not found"));
            
    User retailerObj = userRepository.findById(request.getWarehouseId().longValue())
            .orElseThrow(() -> new RuntimeException("Retailer not found"));

    Delivery delivery = new Delivery();
    // FIXED: Using setShipment and setRetailer with objects
    delivery.setShipment(shipmentObj);
    delivery.setRetailer(retailerObj);
    delivery.setDate(request.getDeliveryDate()); 
    delivery.setStatus("DELIVERED");
    
    deliveryRepository.save(delivery);
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