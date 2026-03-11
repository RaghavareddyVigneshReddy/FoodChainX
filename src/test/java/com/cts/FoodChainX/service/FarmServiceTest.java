package com.cts.FoodChainX.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
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
import com.cts.FoodChainX.model.Role;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.FarmRepository;
import com.cts.FoodChainX.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FarmService farmService;

    private User sampleUser;
    private Farm sampleFarm;
    private final String USER_EMAIL = "farmer@example.com";
    private final Long FARM_ID = 500L;

    @BeforeEach
    void setUp() {
        // 1. Minimum User details needed for Security/Identity logic
        sampleUser = new User();
        sampleUser.setUserId(1L); 
        sampleUser.setEmail(USER_EMAIL); 
        sampleUser.setRole(Role.FARMER);

        // 2. Minimum Farm details needed for Ownership logic
        sampleFarm = new Farm();
        sampleFarm.setFarmId(FARM_ID);
        sampleFarm.setName("Organic Acres");
        
        // Linking the objects is the "Key" to the IDOR test
        sampleFarm.setFarmer(sampleUser); 
        sampleFarm.setCertificationStatus("PENDING");
    }

    // --- 1. CREATE TEST ---
    @Test
    void testRegisterFarm_Success() {
        FarmRequestDto request = new FarmRequestDto("Organic Acres", "Texas");
        
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(sampleUser));
        when(farmRepository.save(any(Farm.class))).thenReturn(sampleFarm);

        FarmResponseDto response = farmService.creatingfarm(request, USER_EMAIL);

        assertNotNull(response);
        assertEquals("Organic Acres", response.getName());
        verify(farmRepository).save(any(Farm.class));
    }

    // --- 2. GET (READ) TEST ---
    @Test
    void testGetMyFarms_Success() {
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(sampleUser));
        // Note: The service uses the user's ID (1L) to find their specific farms
        when(farmRepository.findByFarmer_UserId(1L)).thenReturn(List.of(sampleFarm));

        List<FarmResponseDto> result = farmService.getAllFarmsByFarmerEmail(USER_EMAIL);

        assertFalse(result.isEmpty());
        assertEquals(FARM_ID, result.get(0).getFarmId());
    }

    // --- 3. PATCH (UPDATE STATUS) TEST ---
 @Test
void testUpdateStatus_Success() {
    // 1. Arrange
    // Note: We don't need regulatorEmail if the method doesn't accept it
    when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));
    
    sampleFarm.setCertificationStatus("CERTIFIED");
    when(farmRepository.save(any(Farm.class))).thenReturn(sampleFarm);

    // 2. Act - Only pass 2 arguments: ID and Status
    FarmResponseDto response = farmService.updateStatus(FARM_ID, "CERTIFIED");

    // 3. Assert
    assertEquals("CERTIFIED", response.getCertificationStatus());
    verify(farmRepository).save(sampleFarm);
}

// --- 4. DELETE (SECURITY) TEST - HAPPY PATH ---
    @Test
    void testDeleteFarm_Success_AsOwner() {
        // Line 114 was likely one of these. 
        // If your service doesn't call one of these, Mockito fails.
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(sampleUser));
        when(farmRepository.findById(FARM_ID)).thenReturn(Optional.of(sampleFarm));

        String result = farmService.deleteFarm(FARM_ID, USER_EMAIL);

        assertEquals("Farm removed.", result);
        verify(farmRepository, times(1)).delete(sampleFarm);
    }

    // --- 5. DELETE (SECURITY) TEST - IDOR PREVENTION ---
    @Test
    void testDeleteFarm_Forbidden_NotOwner() {
        String hackerEmail = "hacker@evil.com";
        User hackerUser = new User();
        hackerUser.setUserId(99L); 

        // We ONLY mock the user check because the service stops here!
        when(userRepository.findByEmailIgnoreCase(hackerEmail)).thenReturn(Optional.of(hackerUser));
        
        // DO NOT put 'when(farmRepository.findById...)' here. 
        // The service never reaches that line if the user is unauthorized.

        assertThrows(RuntimeException.class, () -> {
            farmService.deleteFarm(FARM_ID, hackerEmail);
        });

        verify(farmRepository, never()).delete(any());
    }
}
