package com.cts.foodchainx.dto.batch;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for capturing Production Batch creation requests.
 * <p>This DTO is used by the client to submit new harvest data. It contains 
 * strict validation annotations to ensure that farm associations are valid 
 * and harvest metrics are realistic.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequestDto {

    /**
     * The unique identifier of the farm where the batch was produced.
     * <p><b>Constraint:</b> Cannot be null. Must correspond to an existing Farm entity.</p>
     */
    @NotNull(message = "Farm ID is required")
    private Long farmId;

    /**
     * The variety of crop being harvested.
     * <p><b>Constraint:</b> Cannot be null, empty, or consist solely of whitespace.</p>
     */
    @NotBlank(message = "Crop type cannot be empty")
    private String cropType;

    /**
     * The total amount produced in this batch.
     * <p><b>Constraint:</b> Cannot be null and must be at least 1.0.</p>
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Double quantity;

    /**
     * The date on which the harvest occurred.
     * <p><b>Constraint:</b> Cannot be null and must be a date in the past or the current date.</p>
     */
    @NotNull(message = "Harvest date is required")
    @PastOrPresent(message = "Harvest date cannot be in the future")
    private LocalDate harvestDate;
}
