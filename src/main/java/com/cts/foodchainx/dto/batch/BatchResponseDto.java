package com.cts.foodchainx.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for providing a high-level summary of a Production Batch.
 * <p>This DTO is typically returned after creation or during list operations where 
 * full batch details are not required. It serves as a lightweight reference 
 * to the batch's identity and its current stage in the quality lifecycle.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponseDto {

    /**
     * The unique database identifier for the production batch.
     * <p><b>Usage:</b> Used by clients to reference this batch in subsequent 
     * quality check or tracking requests.</p>
     */
    private Long batchId;

    /**
     * The current quality assessment of the batch.
     * <p><b>Values:</b> Typically PENDING, PASSED, or REJECTED.</p>
     */
    private String qualityStatus;
}