package com.cts.foodchainx.dto.logistics;

import com.cts.foodchainx.enums.WarehouseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating Warehouse entity records.
 * Contains the logistical parameters required to register a storage facility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDTO {
    /** ID of the distributor user who owns or manages the warehouse */
    private Long distributorId; 
    
    /** Geographic location or address of the facility */
    private String location; 
    
    /** Maximum storage units the warehouse can accommodate */
    private Long capacity; 
    
    /** Operational status (e.g., Active, Full, Maintenance) */
    private WarehouseStatus status;
}