package com.cts.FoodChainX.dto.batch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponseDto {
    private Long batchId;
    private String qualityStatus;
}