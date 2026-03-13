package com.cts.foodchainx.dto.farm;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for capturing Farm registration details from user input.
 * <p>This DTO is used in the Request Body of the Farm registration process.
 * It includes Jakarta Bean Validation constraints to ensure data integrity 
 * before processing by the controller.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FarmRequestDto {

    /**
     * The desired name for the farm.
     * <p><b>Constraint:</b> Cannot be null, empty, or contain only whitespace.</p>
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The physical location or address of the farm.
     * <p><b>Constraint:</b> Cannot be null, empty, or contain only whitespace.</p>
     */
    @NotBlank(message = "Location is required")
    private String location;

}
