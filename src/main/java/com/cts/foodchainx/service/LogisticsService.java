package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Service interface for managing supply chain logistics.
 * <p>
 * Handles the end-to-end shipment lifecycle, from initiation at the farm 
 * to inventory intake at the retail level, ensuring capacity and quality compliance.
 * </p>
 */
public interface LogisticsService {

    /**
     * Initiates a shipment only if the production batch is quality-compliant.
     * * @param request details of the shipment to be started.
     * @return ShipmentResponseDTO containing the assigned ID and tracking status.
     */
    ShipmentResponseDTO initiateShipment(@NonNull ShipmentRequestDTO request);

    /**
     * Updates shipment status (e.g., DELIVERED) and triggers traceability updates.
     * * @param id the shipment ID.
     * @param request the status update details.
     * @return the updated shipment record.
     */
    ShipmentResponseDTO updateShipmentStatus(@NonNull Long id, @NonNull ShipmentStatusUpdateRequest request);

    /**
     * Records a delivery, validates warehouse capacity, and populates retailer inventory.
     * * @param request delivery data including shipment and warehouse IDs.
     * @throws WarehouseCapacityException if the target warehouse is FULL.
     */
    void recordDelivery(@NonNull DeliveryRequestDTO request);
    WarehouseResponseDTO registerWarehouse(@NonNull WarehouseRequestDTO request);

    /**
     * Retrieves all warehouses currently registered in the system.
     * * @return a list of all warehouse locations and their current status.
     */
    List<WarehouseResponseDTO> getAllWarehouses();
}