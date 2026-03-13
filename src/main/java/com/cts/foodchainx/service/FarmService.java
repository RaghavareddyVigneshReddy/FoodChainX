package com.cts.foodchainx.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.farm.FarmRequestDto;
import com.cts.foodchainx.dto.farm.FarmResponseDto;
import com.cts.foodchainx.exception.FarmNotFoundException;
import com.cts.foodchainx.model.Farm;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.FarmRepository;
import com.cts.foodchainx.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing Farm operations.
 * Handles the business logic for registering, updating, and securing farm data
 * within the food supply chain system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    /**
     * Registers a new farm and links it to an existing user (farmer).
     * <p>The farm is created with an initial "PENDING" certification status.</p>
     * * @param request The DTO containing farm details (name, location).
     * @param email   The email of the authenticated user who will own the farm.
     * @return FarmResponseDto representing the newly created farm.
     * @throws RuntimeException if the user with the provided email is not found.
     */
    @Auditable(action = "CREATE_FARM", resource = "FARM")
    public FarmResponseDto creatingfarm(FarmRequestDto request, String email) {
        User farmer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Farm farmEntity = new Farm();
        farmEntity.setName(request.getName());
        farmEntity.setLocation(request.getLocation());
        farmEntity.setCertificationStatus("PENDING");
        farmEntity.setFarmer(farmer); 

        Farm savedFarm = farmRepository.save(farmEntity);
        return mapToResponseDto(savedFarm);
    }

    /**
     * Retrieves all farms associated with a specific user based on their email.
     * * @param email The email of the farmer.
     * @return List of FarmResponseDto objects.
     * @throws RuntimeException if the farmer account is not found.
     */
    public List<FarmResponseDto> getAllFarmsByFarmerEmail(String email) {
        User farmer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Farmer not found with email: " + email));
        List<Farm> farms = farmRepository.findByFarmer_UserId(farmer.getUserId());
        return farms.stream()
                    .map(this::mapToResponseDto)
                    .toList();
    }

    /**
     * Retrieves details of a specific farm by its primary ID.
     * * @param farmId Unique identifier of the farm.
     * @return FarmResponseDto containing the farm information.
     * @throws FarmNotFoundException if no farm exists with the given ID.
     */
    public FarmResponseDto getFarmById(@NonNull Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        return mapToResponseDto(farm);
    }

    /**
     * Filters and retrieves farms based on their certification status.
     * * @param status The status string to filter by (e.g., APPROVED, PENDING).
     * @return List of FarmResponseDto matching the criteria.
     */
    public List<FarmResponseDto> getFarmsByCertificationStatus(String status) {
        return farmRepository.findByCertificationStatusIgnoreCase(status).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    /**
     * Updates the certification status of a farm. 
     * This method is transactional to ensure data integrity during the update.
     * * @param farmId    The ID of the farm to update.
     * @param newStatus The new status (must be APPROVED, REJECTED, or PENDING).
     * @return FarmResponseDto with the updated status.
     * @throws FarmNotFoundException    if the farm ID is invalid.
     * @throws IllegalArgumentException if the provided status is not supported.
     */
    @Transactional
    @Auditable(action = "UPDATE_FARM_STATUS", resource = "FARM")
    public FarmResponseDto updateStatus(@NonNull Long farmId, String newStatus) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

        String status = newStatus.toUpperCase();
        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Use APPROVED, REJECTED, or PENDING.");
        }

        farm.setCertificationStatus(status);
        return mapToResponseDto(farmRepository.save(farm));
    }

    /**
     * Deletes a farm record from the database.
     * Includes an ownership check to ensure only the owner can delete their farm.
     * * @param farmId The ID of the farm to delete.
     * @param email  The email of the user attempting the deletion.
     * @return A success message string.
     * @throws FarmNotFoundException    if the farm ID is invalid.
     * @throws IllegalArgumentException if the email does not match the farm owner's email.
     */
    @Auditable(action = "DELETE_FARM", resource = "FARM")
    public String deleteFarm(@NonNull Long farmId, String email) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        
        if (!farm.getFarmer().getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Unauthorized: You do not own this farm.");
        }
        
        farmRepository.delete(farm);
        return "Farm removed.";
    }

    /**
     * Helper method to transform a Farm Entity into a FarmResponseDto.
     * * @param farm The Entity object from the database.
     * @return The DTO object for the controller response.
     */
    private FarmResponseDto mapToResponseDto(Farm farm) {
        FarmResponseDto dto = new FarmResponseDto();
        dto.setFarmId(farm.getFarmId());
        dto.setName(farm.getName());
        dto.setLocation(farm.getLocation());
        dto.setCertificationStatus(farm.getCertificationStatus());
        return dto;
    }
}