package com.cts.foodchainx.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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
    private String status; 
    private LocalDate departureDate; 
    private LocalDate arrivalDate; 
}