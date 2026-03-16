package com.cts.foodchainx.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cts.foodchainx.dto.farm.FarmRequestDto;
import com.cts.foodchainx.dto.farm.FarmResponseDto;
import com.cts.foodchainx.enums.CertificationStatus;
import com.cts.foodchainx.service.FarmService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FarmControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FarmService farmService;

    @InjectMocks
    private FarmController farmController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private FarmResponseDto mockResponse;
    private FarmRequestDto mockRequest;
    
    // Use the actual Authentication type that the Controller expects
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(farmController).build();
        
        // Create a real Authentication object with the user's email
        mockAuth = new UsernamePasswordAuthenticationToken("test@user.com", null);

        mockRequest = new FarmRequestDto("Green Valley", "California"); 
        mockResponse = new FarmResponseDto(1L, "Green Valley", "California", "PENDING");
    }

    @Test
    void registerFarm_ShouldReturnOk() throws Exception {
        when(farmService.creatingfarm(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/farms/register")
                .principal(mockAuth) // Use the Authentication object here
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Green Valley"));
    }

    @Test
    void getMyFarms_ShouldReturnList() throws Exception {
        when(farmService.getAllFarmsByFarmerEmail(any())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/farms/my-farms")
                .principal(mockAuth)) 
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Green Valley"));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedFarm() throws Exception {
        mockResponse.setCertificationStatus("APPROVED");
        when(farmService.updateStatus(anyLong(), any())).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/farms/1/status")
                .param("status", "APPROVED"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.certificationStatus").value("APPROVED"));
    }

    @Test
    void removeFarm_ShouldReturnSuccessMessage() throws Exception {
        String successMsg = "Farm deleted successfully";
        when(farmService.deleteFarm(anyLong(), any())).thenReturn(successMsg);

        mockMvc.perform(delete("/api/farms/1")
                .principal(mockAuth)) 
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(successMsg));
    }
}
