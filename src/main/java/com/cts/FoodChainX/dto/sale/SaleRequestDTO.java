package com.cts.foodchainx.dto.sale;

import lombok.Data;

@Data
public class SaleRequestDTO {

    private Long inventoryId;

    private Long consumerId;

    private Long quantity;

    private Double price;

}
