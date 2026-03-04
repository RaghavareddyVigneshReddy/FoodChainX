package com.cts.FoodChainX.dto.batch;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponseDto {
    private Long batchId;
    private LocalDate harvestDate;
    private String qualityStatus;
}