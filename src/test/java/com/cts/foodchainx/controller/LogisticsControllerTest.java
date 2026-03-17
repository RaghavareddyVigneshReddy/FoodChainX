package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.service.LogisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LogisticsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LogisticsService logisticsService;

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/logistics/shipments - Success")
    @WithMockUser(roles = "LOGISTICS_MANAGER")
    void createShipment_Success() throws Exception {
        ShipmentRequestDTO request = new ShipmentRequestDTO(); 
        ShipmentResponseDTO response = new ShipmentResponseDTO();
        // Set DTO fields as per your implementation
        
        when(logisticsService.initiateShipment(any(ShipmentRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/logistics/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("PUT /api/logistics/shipments/{id}/status - Success")
    @WithMockUser(roles = "LOGISTICS_MANAGER")
    void updateStatus_Success() throws Exception {
        Long shipmentId = 1L;
        ShipmentStatusUpdateRequest updateRequest = new ShipmentStatusUpdateRequest();
        ShipmentResponseDTO response = new ShipmentResponseDTO();

        when(logisticsService.updateShipmentStatus(eq(shipmentId), any())).thenReturn(response);

        mockMvc.perform(put("/api/logistics/shipments/{id}/status", shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET /api/logistics/warehouses - Success")
    @WithMockUser(roles = "LOGISTICS_MANAGER")
    void getWarehouses_Success() throws Exception {
        WarehouseResponseDTO warehouse = new WarehouseResponseDTO();
        when(logisticsService.getAllWarehouses()).thenReturn(List.of(warehouse));

        mockMvc.perform(get("/api/logistics/warehouses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/logistics/deliveries - Forbidden for FARMER")
    @WithMockUser(roles = "FARMER")
    void logDelivery_Forbidden() throws Exception {
        DeliveryRequestDTO request = new DeliveryRequestDTO();

        mockMvc.perform(post("/api/logistics/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/logistics/deliveries - Success for RETAILER")
    @WithMockUser(roles = "RETAILER")
    void logDelivery_Success() throws Exception {
        DeliveryRequestDTO request = new DeliveryRequestDTO();

        mockMvc.perform(post("/api/logistics/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Delivery recorded successfully"));
    }
}
