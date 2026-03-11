package com.cts.FoodChainX.dto.batch;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Keep this for frameworks like Jackson
public class BatchDetailResponseDto {
    private Long batchId;
    private String cropType;
    private Double quantity;
    private String qualityStatus;
    private List<String> inspectionFindings; 
    private String farmName;
    private String farmLocation;

    // Manually adding the All-Args Constructor to fix the compiler error
    public BatchDetailResponseDto(Long batchId, String cropType, Double quantity, 
                                 String qualityStatus, List<String> inspectionFindings, 
                                 String farmName, String farmLocation) {
        this.batchId = batchId;
        this.cropType = cropType;
        this.quantity = quantity;
        this.qualityStatus = qualityStatus;
        this.inspectionFindings = inspectionFindings;
        this.farmName = farmName;
        this.farmLocation = farmLocation;
    }
}