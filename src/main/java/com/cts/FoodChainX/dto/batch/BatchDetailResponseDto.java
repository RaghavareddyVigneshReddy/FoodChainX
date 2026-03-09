package com.cts.FoodChainX.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDetailResponseDto {
    private Long batchId;
    private String cropType;
    private Double quantity;
    private String qualityStatus;
    
    // This pulls from the QualityCheck table
    private List<String> inspectionFindings; 
    
    // You can even add Farm info here for the Consumer!
    private String farmName;
    private String farmLocation;
}
