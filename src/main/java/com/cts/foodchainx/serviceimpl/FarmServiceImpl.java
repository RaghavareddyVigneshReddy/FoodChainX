package com.cts.foodchainx.serviceimpl;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.farm.FarmRequestDto;
import com.cts.foodchainx.dto.farm.FarmResponseDto;
import com.cts.foodchainx.enums.CertificationStatus;
import com.cts.foodchainx.exception.FarmNotFoundException;
import com.cts.foodchainx.model.Farm;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.FarmRepository;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.FarmService; // IMPORT THE INTERFACE HERE

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link FarmService} interface.
 * Handles the concrete business logic for farm operations including
 * registration, status updates, and ownership-validated deletion.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     * <p>Finds the user by email, initializes a new Farm entity with PENDING status,
     * and persists it to the database.</p>
     */
    @Override
    @Auditable(action = "CREATE_FARM", resource = "FARM")
    public FarmResponseDto creatingfarm(FarmRequestDto request, String email) {
        log.info("Creating a new farm for user: {}", email);
        User farmer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Farm farmEntity = new Farm();
        farmEntity.setName(request.getName());
        farmEntity.setLocation(request.getLocation());
        farmEntity.setCertificationStatus(CertificationStatus.PENDING);
        farmEntity.setFarmer(farmer); 

        Farm savedFarm = farmRepository.save(farmEntity);
        return mapToResponseDto(savedFarm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FarmResponseDto> getAllFarmsByFarmerEmail(String email) {
        log.debug("Fetching all farms for farmer: {}", email);
        User farmer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Farmer not found with email: " + email));
        
        List<Farm> farms = farmRepository.findByFarmer_UserId(farmer.getUserId());
        return farms.stream()
                    .map(this::mapToResponseDto)
                    .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FarmResponseDto getFarmById(@NonNull Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        return mapToResponseDto(farm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FarmResponseDto> getFarmsByCertificationStatus(CertificationStatus status) {
        return farmRepository.findByCertificationStatus(status).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>Updates the status field of a farm entity. Marked as {@code @Transactional} 
     * to ensure the update is committed safely.</p>
     */
    @Override
    @Transactional
    @Auditable(action = "UPDATE_FARM_STATUS", resource = "FARM")
    public FarmResponseDto updateStatus(@NonNull Long farmId, CertificationStatus newStatus) {
        log.info("Updating status for Farm ID: {} to {}", farmId, newStatus);
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

        farm.setCertificationStatus(newStatus);
        return mapToResponseDto(farmRepository.save(farm));
    }

    /**
     * {@inheritDoc}
     * <p>Performs a security check to ensure the requesting user's email matches 
     * the farm owner's email before deletion.</p>
     */
    @Override
    @Auditable(action = "DELETE_FARM", resource = "FARM")
    public String deleteFarm(@NonNull Long farmId, String email) {
        log.warn("Attempting to delete Farm ID: {} by user: {}", farmId, email);
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        
        if (!farm.getFarmer().getEmail().equalsIgnoreCase(email)) {
            log.error("Unauthorized deletion attempt for Farm ID: {} by user: {}", farmId, email);
            throw new IllegalArgumentException("Unauthorized: You do not own this farm.");
        }
        
        farmRepository.delete(farm);
        return "Farm removed.";
    }

    /**
     * Internal helper method to convert a {@link Farm} entity into a {@link FarmResponseDto}.
     *
     * @param farm The entity to map.
     * @return The mapped DTO.
     */
    private FarmResponseDto mapToResponseDto(Farm farm) {
        FarmResponseDto dto = new FarmResponseDto();
        dto.setFarmId(farm.getFarmId());
        dto.setName(farm.getName());
        dto.setLocation(farm.getLocation());
        dto.setCertificationStatus(farm.getCertificationStatus().name());
        return dto;
    }
}