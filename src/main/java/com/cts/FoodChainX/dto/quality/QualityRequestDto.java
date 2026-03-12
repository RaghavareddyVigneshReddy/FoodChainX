package com.cts.foodchainx.dto.quality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityRequestDto {
    private Long batchId;
    private Long inspectorId;
    private String findings;
    private String status;
}
