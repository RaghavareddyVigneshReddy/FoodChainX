package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.service.LogisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing logistics and supply chain operations.
 * Provides endpoints for creating shipments, updating statuses, and tracking warehouse data.
 */
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    /**
     * Initiates a new shipment for a specific production batch.
     * * @param request the shipment details including batch and distributor IDs
     * @return the created shipment details with HTTP 201 status
     */
    @PostMapping("/shipments")
    public ResponseEntity<ShipmentResponseDTO> createShipment(
            @Valid @RequestBody @NonNull ShipmentRequestDTO request) {
        return new ResponseEntity<>(logisticsService.initiateShipment(request), HttpStatus.CREATED);
    }

    /**
     * Updates the status of an existing shipment.
     * * @param id the unique identifier of the shipment
     * @param request the new status update
     * @return the updated shipment details
     */
    @PutMapping("/shipments/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(
            @PathVariable @NonNull Long id, 
            @Valid @RequestBody @NonNull ShipmentStatusUpdateRequest request) {
        return ResponseEntity.ok(logisticsService.updateShipmentStatus(id, request));
    }

    /**
     * Registers a new warehouse facility in the supply chain.
     * <p>
     * maximum capacity. The warehouse is initialized with a current stock level of zero 
     * and set to an AVAILABLE status by default.
     * </p>
     * * @param request the warehouse details including distributor ID, location, and capacity
     * @return the created WarehouseResponseDTO with assigned ID and status, with HTTP 201 status
     */
    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(
        @Valid @RequestBody @NonNull WarehouseRequestDTO request) {
            return new ResponseEntity<>(logisticsService.registerWarehouse(request), HttpStatus.CREATED);
    }
    

    /**
     * Retrieves a list of all registered warehouses.
     * * @return a list of WarehouseResponseDTOs
     */
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getWarehouses() {
        return ResponseEntity.ok(logisticsService.getAllWarehouses());
    }

    /**
     * Records a successful delivery at a retailer location and updates inventory.
     * * @param request the delivery details including shipment and retailer IDs
     * @return a success message with HTTP 201 status
     */
    @PostMapping("/deliveries")
    public ResponseEntity<String> logDelivery(@Valid @RequestBody @NonNull DeliveryRequestDTO request) {
        logisticsService.recordDelivery(request);
        return new ResponseEntity<>("Delivery recorded successfully", HttpStatus.CREATED);
    }
}