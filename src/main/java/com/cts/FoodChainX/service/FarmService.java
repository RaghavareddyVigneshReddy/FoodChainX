package com.cts.FoodChainX.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.repository.FarmRepository;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    /**
     * 1. CREATE: Register a new farm plot
     */
    public FarmResponseDto creatingfarm(FarmRequestDto request, Long farmerId) {
        Farm farmEntity = new Farm();
        farmEntity.setName(request.getName());
        farmEntity.setLocation(request.getLocation());
        farmEntity.setCertificationStatus("PENDING"); // Default logic
        farmEntity.setFarmerId(farmerId);             // Security link

        Farm savedFarm = farmRepository.save(farmEntity);
        return mapToResponseDto(savedFarm);
    }

    /**
     * 2. READ: Get all farms for a specific farmer
     */
    public List<FarmResponseDto> getAllFarmsByFarmer(Long farmerId) {
        List<Farm> farms = farmRepository.findByFarmerId(farmerId);
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
     * 4. PATCH: Update certification status (Admin/Inspector only)
     */
    public FarmResponseDto updateStatus(Long farmId, String newStatus) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        
        farm.setCertificationStatus(newStatus);
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

    /**
     * HELPER: Private method to convert Entity to DTO
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
