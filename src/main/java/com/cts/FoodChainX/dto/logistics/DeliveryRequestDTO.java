package com.cts.FoodChainX.dto.logistics;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DeliveryRequestDTO {
    private Long shipmentId;
    private Long warehouseId;
    private LocalDate deliveryDate;
}