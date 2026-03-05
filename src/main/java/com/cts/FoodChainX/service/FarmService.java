package com.cts.FoodChainX.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.UserRepository;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserRepository userRepository; // Needed to fetch the User object

    /**
     * 1. CREATE: Register a new farm plot linked to a User object
     */
    public FarmResponseDto creatingfarm(FarmRequestDto request, Long farmerId) {
        // Find the farmer in the database first
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + farmerId));

        Farm farmEntity = new Farm();
        farmEntity.setName(request.getName());
        farmEntity.setLocation(request.getLocation());
        farmEntity.setCertificationStatus("PENDING");
        
        // Use setFarmer(User) instead of setFarmerId(Long)
        farmEntity.setFarmer(farmer); 

        Farm savedFarm = farmRepository.save(farmEntity);
        return mapToResponseDto(savedFarm);
    }

    /**
     * 2. READ: Get all farms for a specific farmer
     */
    public List<FarmResponseDto> getAllFarmsByFarmer(Long farmerId) {
        // Matches the method name in your FarmRepository
        List<Farm> farms = farmRepository.findByFarmer_UserId(farmerId);
        return farms.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
    }

    /**
     * 3. READ: Get details of a specific farm
     */
    public FarmResponseDto getFarmById(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found with ID: " + farmId));
        return mapToResponseDto(farm);
    }

    /**
  /**
     * 4. READ: Get all farms filtered by certification status (e.g., PENDING)
     * Used by Regulators to FIND which farms need auditing.
     */
    public List<FarmResponseDto> getFarmsByCertificationStatus(String status) {
        return farmRepository.findByCertificationStatusIgnoreCase(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 5. PATCH: Update certification status
     * Used by Regulators to APPROVE or REJECT a farm after an audit.
     */
    public FarmResponseDto updateStatus(Long farmId, String newStatus) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found with ID: " + farmId));
        
        farm.setCertificationStatus(newStatus); // Standardizes the certification status
        Farm updatedFarm = farmRepository.save(farm);
        return mapToResponseDto(updatedFarm);
    }

    /**
     * 5. DELETE: Remove a farm record
     */
    public String deleteFarm(Long farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new RuntimeException("Cannot delete. Farm ID " + farmId + " not found.");
        }
        farmRepository.deleteById(farmId);
        return "Farm successfully removed from the system.";
    }

    private FarmResponseDto mapToResponseDto(Farm farm) {
        FarmResponseDto dto = new FarmResponseDto();
        dto.setFarmId(farm.getFarmId());
        dto.setName(farm.getName());
        dto.setLocation(farm.getLocation());
        dto.setCertificationStatus(farm.getCertificationStatus());
        return dto;
    }
}