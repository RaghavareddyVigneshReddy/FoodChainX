package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.ShipmentStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LogisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LogisticsService logisticsService;

    @Test
    @DisplayName("POST /api/logistics/warehouses - Success when DISTRIBUTOR")
    @WithMockUser(roles = "DISTRIBUTOR")
    void createWarehouse_Success() throws Exception {
        WarehouseRequestDTO dto = new WarehouseRequestDTO();
        dto.setLocation("Warehouse A");
        dto.setCapacity(5000L);
        dto.setDistributorId(2L);

        WarehouseResponseDTO response = WarehouseResponseDTO.builder()
                .warehouseId(1L)
                .location("Warehouse A")
                .build();

        when(logisticsService.registerWarehouse(any(WarehouseRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/logistics/warehouses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warehouseId").value(1))
                .andExpect(jsonPath("$.location").value("Warehouse A"));
    }

    @Test
    @DisplayName("PUT /api/logistics/shipments/{id}/status - Update status")
    @WithMockUser(roles = "DISTRIBUTOR")
    void updateShipmentStatus_Success() throws Exception {
        ShipmentStatusUpdateRequest updateRequest = new ShipmentStatusUpdateRequest();
        updateRequest.setStatus(ShipmentStatus.DELIVERED);

        ShipmentResponseDTO response = ShipmentResponseDTO.builder()
                .shipmentId(1L)
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(logisticsService.updateShipmentStatus(any(Long.class), any())).thenReturn(response);

        mockMvc.perform(put("/api/logistics/shipments/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}