package com.cts.foodchainx.dto.inventory;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for creating or updating inventory records.
 * <p>
 * This class captures the minimum required information from the client
 * to register inventory at a retail location. It excludes system-generated
 * fields like {@code status} and {@code dateAdded}.
 * </p>
 */
@Data
public class InventoryRequestDTO {

    /**
     * The unique identifier of the retailer receiving the stock.
     */
    private Long retailerId;

    /**
     * The unique identifier of the production batch being added to inventory.
     */
    private Long batchId;

    /**
     * The number of units being added to the retailer's stock.
     */
    private Long quantity;
}