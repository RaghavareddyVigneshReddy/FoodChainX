package com.cts.FoodChainX.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequestDto {
    private Long farmId;
    private String cropType;
    private Double quantity;
}
