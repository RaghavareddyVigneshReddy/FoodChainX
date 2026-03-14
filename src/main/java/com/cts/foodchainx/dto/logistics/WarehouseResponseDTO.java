package com.cts.foodchainx.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the details of a warehouse facility.
 * Used for sending warehouse information in API responses to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {

    /** The unique identifier of the warehouse record */
    private Long warehouseId;

    /** The geographic location or physical address of the facility */
    private String location;

    /** The maximum storage capacity of the facility */
    private Long capacity;

    /** The current status of the facility (e.g., Available, Full, Inactive) */
    private String status;
}