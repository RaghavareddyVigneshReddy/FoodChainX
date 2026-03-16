package com.cts.foodchainx.dto.quality;

import com.cts.foodchainx.enums.QualityStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for submitting a quality inspection report.
 * <p>This DTO is used by regulators or inspectors to log their findings 
 * and set the final quality status for a specific production batch.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityRequestDto {

    /**
     * The unique identifier of the production batch being inspected.
     * <p><b>Requirement:</b> Must match an existing record in the PRODUCTION table.</p>
     */
    private Long batchId;

    /**
     * The unique identifier of the User performing the inspection.
     * <p><b>Requirement:</b> Must correspond to a user with the appropriate inspector/regulator role.</p>
     */
    private Long inspectorId;

    /**
     * Detailed observations, laboratory results, or comments regarding the batch quality.
     * <p><b>Note:</b> This information is stored as a Large Object (CLOB/TEXT) in the database.</p>
     */
    private String findings;

    /**
     * The resulting status determined by the inspection.
     * <p><b>Expected Values:</b> PASSED, REJECTED, or PENDING.</p>
     */
    private QualityStatus status;
}
