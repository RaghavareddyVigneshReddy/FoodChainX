package com.cts.foodchainx.dto.quality;

import java.time.LocalDate;

import com.cts.foodchainx.enums.QualityStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for returning quality inspection results to the client.
 * <p>This DTO provides a read-only summary of an inspection event. It is 
 * commonly used in history logs or detail views to show when a check occurred 
 * and what the specific findings were.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityResponseDto {

    /**
     * The unique system identifier for the quality check record.
     * <p><b>Usage:</b> This ID is used for tracking or specifically referencing 
     * this historical log entry.</p>
     */
    private Long qualityId;

    /**
     * The date on which the inspection was officially recorded.
     * <p><b>Format:</b> ISO-8601 local date (YYYY-MM-DD).</p>
     */
    private LocalDate date;

    /**
     * The final result of the quality assessment.
     * <p><b>Common Values:</b> PASSED, REJECTED, NEEDS_RECHECK.</p>
     */
    private QualityStatus status;   

    /**
     * The descriptive notes or laboratory findings provided by the inspector.
     * <p><b>Note:</b> This may contain detailed text regarding compliance or 
     * reasons for rejection.</p>
     */
    private String findings;
}
