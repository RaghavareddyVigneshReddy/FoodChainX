package com.cts.FoodChainX.dto.inventory;

import lombok.Data;

@Data
public class InventoryRequestDTO {

    private Long retailerId;
    private Long batchId;
    private Long quantity;
}
