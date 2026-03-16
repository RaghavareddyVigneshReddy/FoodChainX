package com.cts.foodchainx.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.cts.foodchainx.enums.ShipmentStatus;

/**
 * Response DTO containing shipment details for the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO {
    private Long shipmentId; 
    private Long batchId; 
    private Long distributorId;
    private ShipmentStatus status; 
    private LocalDate departureDate; 
    private LocalDate arrivalDate; 
}