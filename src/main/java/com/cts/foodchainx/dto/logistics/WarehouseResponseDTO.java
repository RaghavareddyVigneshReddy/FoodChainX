package com.cts.foodchainx.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {
    private Long warehouseId;
    private String location;
    private Long capacity;
    private String status;
}