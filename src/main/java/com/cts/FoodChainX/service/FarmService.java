package com.cts.FoodChainX.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.aspect.Auditable; // Keep this import
import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.exception.FarmNotFoundException;
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.UserRepository;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1. CREATE: Register a new farm plot linked to a User object
     */
    @Auditable(action = "CREATE_FARM", resource = "FARM") // Added from main
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
     * 2. READ: Get all farms for a specific farmer
     */
    public List<FarmResponseDto> getAllFarmsByFarmerEmail(String email) {
        User farmer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Farmer not found with email: " + email));
        List<Farm> farms = farmRepository.findByFarmer_UserId(farmer.getUserId());
        return farms.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
    }

    /**
     * 3. READ: Get details of a specific farm
     */
    public FarmResponseDto getFarmById(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        return mapToResponseDto(farm);
    }
    /**
     * 4. READ: Get all farms filtered by certification status
     */
    public List<FarmResponseDto> getFarmsByCertificationStatus(String status) {
        return farmRepository.findByCertificationStatusIgnoreCase(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 5. PATCH: Update certification status with STRICT validation
     */
    @Transactional
    @Auditable(action = "UPDATE_FARM_STATUS", resource = "FARM") // Logic 2 + Audit
    public FarmResponseDto updateStatus(Long farmId, String newStatus) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

        // Keep logic 2: Strict Validation
        String status = newStatus.toUpperCase();
        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("PENDING")) {
            throw new RuntimeException("Invalid status. Use APPROVED, REJECTED, or PENDING.");
        }

        farm.setCertificationStatus(status);
        return mapToResponseDto(farmRepository.save(farm));
    }

    /**
     * 6. DELETE: Remove a farm record with Ownership check
     */
    @Auditable(action = "DELETE_FARM", resource = "FARM") // Logic 2 + Audit
    public String deleteFarm(Long farmId, String email) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
        
        if (!farm.getFarmer().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this farm.");
        }
        
        farmRepository.delete(farm);
        return "Farm removed.";
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