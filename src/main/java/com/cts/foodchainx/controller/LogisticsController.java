package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.service.LogisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // Added for null safety
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor // Fix S6813: Enables Constructor Injection
public class LogisticsController {

    // Removed @Autowired (Field Injection is a code smell)
    // Using 'final' ensures the service is injected at construction time
    private final LogisticsService logisticsService;

    @PostMapping("/shipments")
    public ResponseEntity<ShipmentResponseDTO> createShipment(
            @Valid @RequestBody @NonNull ShipmentRequestDTO request) {
        // Fix: Added @NonNull to request to satisfy type safety
        return new ResponseEntity<>(logisticsService.initiateShipment(request), HttpStatus.CREATED);
    }

    @PutMapping("/shipments/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(
            @PathVariable @NonNull Long id, 
            @Valid @RequestBody @NonNull ShipmentStatusUpdateRequest request) {
        // Fix: Added @NonNull to both id and request
        return ResponseEntity.ok(logisticsService.updateShipmentStatus(id, request));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getWarehouses() {
        return ResponseEntity.ok(logisticsService.getAllWarehouses());
    }

    @PostMapping("/deliveries")
    public ResponseEntity<String> logDelivery(@Valid @RequestBody @NonNull DeliveryRequestDTO request) {
        // Fix: Logic is cleaner if we let the GlobalExceptionHandler handle the errors
        logisticsService.recordDelivery(request);
        return new ResponseEntity<>("Delivery recorded successfully", HttpStatus.CREATED);
    }
}