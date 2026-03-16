package com.cts.foodchainx.service;

import java.util.List;

import org.springframework.lang.NonNull;

import com.cts.foodchainx.dto.farm.FarmRequestDto;
import com.cts.foodchainx.dto.farm.FarmResponseDto;
import com.cts.foodchainx.enums.CertificationStatus;

/**
 * Service Interface for managing Farm operations within the food supply chain.
 * <p>
 * Provides a contract for business logic related to farm registration, 
 * retrieval by various criteria, status updates, and deletion.
 * </p>
 */
public interface FarmService {

    /**
     * Registers a new farm in the system and links it to a farmer's profile.
     *
     * @param request The {@link FarmRequestDto} containing the farm's name and location.
     * @param email   The email address of the authenticated user (farmer) registering the farm.
     * @return A {@link FarmResponseDto} containing the details of the created farm.
     */
    FarmResponseDto creatingfarm(FarmRequestDto request, String email);

    /**
     * Retrieves all farms owned by a specific farmer.
     *
     * @param email The email address of the farmer whose farms are to be fetched.
     * @return A {@link List} of {@link FarmResponseDto} objects associated with the email.
     */
    List<FarmResponseDto> getAllFarmsByFarmerEmail(String email);

    /**
     * Fetches details of a specific farm by its unique identifier.
     *
     * @param farmId The unique ID of the farm. Must not be null.
     * @return A {@link FarmResponseDto} representing the requested farm.
     * @throws com.cts.foodchainx.exception.FarmNotFoundException if no farm is found with the given ID.
     */
    FarmResponseDto getFarmById(@NonNull Long farmId);

    /**
     * Filters and retrieves a list of farms based on their certification status.
     * Useful for regulators to find all "PENDING" or "APPROVED" farms.
     *
     * @param status The {@link CertificationStatus} (e.g., PENDING, APPROVED, REJECTED).
     * @return A {@link List} of {@link FarmResponseDto} matching the specified status.
     */
    List<FarmResponseDto> getFarmsByCertificationStatus(CertificationStatus status);

    /**
     * Updates the certification status of an existing farm.
     *
     * @param farmId    The unique ID of the farm to update. Must not be null.
     * @param newStatus The new {@link CertificationStatus} to be applied.
     * @return A {@link FarmResponseDto} containing the updated information.
     */
    FarmResponseDto updateStatus(@NonNull Long farmId, CertificationStatus newStatus);

    /**
     * Deletes a farm record from the system. 
     * Implementations should verify that the user requesting deletion is the owner.
     *
     * @param farmId The unique ID of the farm to be removed. Must not be null.
     * @param email  The email of the user attempting the deletion for authorization checks.
     * @return A success message string upon successful deletion.
     */
    String deleteFarm(@NonNull Long farmId, String email);
}