package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.logistics.*;
import com.cts.FoodChainX.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

   

    @PostMapping("/shipments")
    public ResponseEntity<ShipmentResponseDTO> createShipment(@Valid @RequestBody ShipmentRequestDTO request) {
        // Initiates shipment for Compliant batches only
        return new ResponseEntity<>(logisticsService.initiateShipment(request), HttpStatus.CREATED);
    }

    @PutMapping("/shipments/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(
            @PathVariable Long id, 
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        // Updates real-time movement status
        return ResponseEntity.ok(logisticsService.updateShipmentStatus(id, request));
    }

   

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getWarehouses() {
        // Returns capacity and location details for monitoring
        return ResponseEntity.ok(logisticsService.getAllWarehouses());
    }

    @PostMapping("/deliveries")
    public ResponseEntity<String> logDelivery(@Valid @RequestBody DeliveryRequestDTO request) {
        try {
            // Records the delivery to the retailer/warehouse
            logisticsService.recordDelivery(request);
            return new ResponseEntity<>("Delivery recorded successfully", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            
            if (e.getMessage().contains("409")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}