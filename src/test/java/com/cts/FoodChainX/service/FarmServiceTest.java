package com.cts.FoodChainX.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.model.Farm;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FarmService farmService;

    private User sampleUser;
    private Farm sampleFarm;
    private final String EMAIL = "farmer@example.com";
    private final Long FARM_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(101L);
        sampleUser.setEmail(EMAIL);

        sampleFarm = new Farm();
        sampleFarm.setFarmId(FARM_ID);
        sampleFarm.setName("Green Valley");
        sampleFarm.setLocation("Texas");
        sampleFarm.setCertificationStatus("PENDING");
        sampleFarm.setFarmer(sampleUser);
    }

    // 1. Test Creating a Farm
    @Test
    @DisplayName("Create Farm - Success")
    void testCreatingFarm_Success() {
        FarmRequestDto request = new FarmRequestDto("Green Valley", "Texas");
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(sampleUser));
        when(farmRepository.save(any(Farm.class))).thenReturn(sampleFarm);

        FarmResponseDto response = farmService.creatingfarm(request, EMAIL);

        assertNotNull(response);
        assertEquals("Green Valley", response.getName());
        verify(farmRepository, times(1)).save(any(Farm.class));
    }

    // 2. Test Get All Farms by Farmer Email
    @Test
    @DisplayName("Get All Farms by Email - Success")
    void testGetAllFarmsByEmail() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(sampleUser));
        when(farmRepository.findByFarmer_UserId(101L)).thenReturn(List.of(sampleFarm));

        List<FarmResponseDto> result = farmService.getAllFarmsByFarmerEmail(EMAIL);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // 3. Test Update Status - SUCCESS (Using APPROVED)
    @Test
    @DisplayName("Update Status - Success with APPROVED")
    void testUpdateStatus_Success() {
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));
        when(farmRepository.save(any(Farm.class))).thenReturn(sampleFarm);

        FarmResponseDto response = farmService.updateStatus(FARM_ID, "APPROVED");

        assertEquals("APPROVED", response.getCertificationStatus());
    }

    // 4. Test Update Status - FAILURE (Validation Logic)
    @Test
    @DisplayName("Update Status - Error when using anything except APPROVED, PENDING, REJECTED")
    void testUpdateStatus_InvalidStatus() {
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));

        // This ensures your code throws an error if status is NOT one of the 3 allowed words
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            farmService.updateStatus(FARM_ID, "CERTIFIED"); // CERTIFIED is not allowed
        });

        assertTrue(exception.getMessage().contains("Invalid status"));
        verify(farmRepository, never()).save(any());
    }

    // 5. Test Delete Farm - Success (Owner)
    @Test
    @DisplayName("Delete Farm - Success as Owner")
    void testDeleteFarm_Success() {
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));

        String result = farmService.deleteFarm(FARM_ID, EMAIL);

        assertEquals("Farm removed.", result);
        verify(farmRepository, times(1)).delete(sampleFarm);
    }

    // 6. Test Delete Farm - Unauthorized (Not Owner)
    @Test
    @DisplayName("Delete Farm - Unauthorized if user is not the owner")
    void testDeleteFarm_Unauthorized() {
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));

        assertThrows(RuntimeException.class, () -> {
            farmService.deleteFarm(FARM_ID, "wrong@email.com");
        });

        verify(farmRepository, never()).delete(any());
    }
}