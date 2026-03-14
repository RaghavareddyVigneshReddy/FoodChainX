package com.cts.foodchainx.dto.batch;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for providing a comprehensive view of a Production Batch.
 * <p>This DTO aggregates data from the ProductionBatch entity, the associated Farm entity, 
 * and the collection of QualityCheck findings. It is primarily used for the detailed 
 * view in the user interface.</p>
 */
@Data
@NoArgsConstructor // Keep this for frameworks like Jackson
public class BatchDetailResponseDto {

    /**
     * The unique identifier for the production batch.
     */
    private Long batchId;

    /**
     * The variety or type of crop in this batch.
     */
    private String cropType;

    /**
     * The total volume or weight of the batch.
     */
    private Double quantity;

    /**
     * The current lifecycle status (e.g., PENDING, PASSED, REJECTED).
     */
    private String qualityStatus;

    /**
     * A collection of text-based findings from all quality inspections performed on this batch.
     * <p><b>Source:</b> Derived from the associated QualityCheck entities.</p>
     */
    private List<String> inspectionFindings; 

    /**
     * The name of the farm where this batch was produced.
     * <p><b>Source:</b> Joined from the Farm entity.</p>
     */
    private String farmName;

    /**
     * The physical address or coordinates of the farm.
     * <p><b>Source:</b> Joined from the Farm entity.</p>
     */
    private String farmLocation;

    /**
     * Manually defined All-Args Constructor to facilitate manual mapping in Service layers.
     * <p>This constructor allows for easy instantiation when converting from 
     * Entity objects to this DTO without requiring Lombok's @AllArgsConstructor.</p>
     * * @param batchId            The production batch ID.
     * @param cropType           The type of crop.
     * @param quantity           Total quantity.
     * @param qualityStatus      Current certification status.
     * @param inspectionFindings List of observations from inspectors.
     * @param farmName           Name of the origin farm.
     * @param farmLocation       Location of the origin farm.
     */
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