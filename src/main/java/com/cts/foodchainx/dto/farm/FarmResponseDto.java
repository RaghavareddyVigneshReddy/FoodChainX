package com.cts.foodchainx.dto.farm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending Farm information back to the client.
 * <p>This DTO provides a "public view" of the Farm entity, including 
 * system-generated fields like the ID and current status that the user 
 * cannot modify directly via the registration request.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FarmResponseDto {

    /**
     * The unique database identifier assigned to the farm.
     * Useful for subsequent API calls (e.g., updating or deleting).
     */
    private Long farmId;

    /**
     * The registered name of the farm.
     */
    private String name;

    /**
     * The geographic location of the farm.
     */
    private String location;

    /**
     * The current lifecycle status of the farm in the system.
     * <p>Possible values include: PENDING, APPROVED, REJECTED.</p>
     */
    private String certificationStatus;
}
