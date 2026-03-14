package com.cts.foodchainx.dto.logistics;

import lombok.Data;

/**
 * DTO for updating the operational status of a shipment.
 * Primarily used by logistics personnel to transition shipments through states like IN_TRANSIT, DELIVERED, or DELAYED.
 */
@Data
public class ShipmentStatusUpdateRequest {
    /** The new status string to be applied to the shipment record */
    private String status;
}