package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.inventory.InventoryRequestDTO;
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.serviceimpl.InventoryServiceImpl;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryServiceImpl inventoryService;

    @Test
    @DisplayName("POST /api/retail/inventory - Success when RETAILER")
    @WithMockUser(roles = "RETAILER")
    void createInventory_Success() throws Exception {
        // Arrange
        InventoryRequestDTO dto = new InventoryRequestDTO();
        dto.setRetailerId(101L);
        dto.setBatchId(501L);
        dto.setQuantity(100L);

        Inventory savedInventory = new Inventory();
        savedInventory.setInventoryId(1L);
        savedInventory.setRetailerId(101L);
        savedInventory.setBatchId(501L);
        savedInventory.setQuantity(100L);

        when(inventoryService.createInventory(any(Inventory.class))).thenReturn(savedInventory);

        // Act & Assert
        mockMvc.perform(post("/api/retail/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(1))
                .andExpect(jsonPath("$.retailerId").value(101))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @DisplayName("GET /api/retail/inventory - Return all items")
    @WithMockUser(roles = "RETAILER")
    void getAllInventory_Success() throws Exception {
        Inventory item = new Inventory();
        item.setInventoryId(1L);

        when(inventoryService.getAllInventory()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/retail/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].inventoryId").value(1));
    }

    @Test
    @DisplayName("GET /api/retail/inventory/{id} - Return specific item")
    @WithMockUser(roles = "RETAILER")
    void getInventoryById_Success() throws Exception {
        Inventory item = new Inventory();
        item.setInventoryId(99L);

        when(inventoryService.getInventoryById(99L)).thenReturn(item);

        mockMvc.perform(get("/api/retail/inventory/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(99));
    }

    @Test
    @DisplayName("GET /api/retail/inventory/retailer/{id} - Filter by retailer")
    @WithMockUser(roles = "RETAILER")
    void getInventoryByRetailer_Success() throws Exception {
        Inventory item = new Inventory();
        item.setRetailerId(101L);

        when(inventoryService.getInventoryByRetailer(101L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/retail/inventory/retailer/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].retailerId").value(101));
    }

    @Test
    @DisplayName("POST /api/retail/inventory - Failure on missing fields (NullPointerException)")
    @WithMockUser(roles = "RETAILER")
    void createInventory_NullField_ThrowsException() throws Exception {
        // RetailerId is missing in this DTO
        InventoryRequestDTO incompleteDto = new InventoryRequestDTO();
        incompleteDto.setBatchId(501L);

        mockMvc.perform(post("/api/retail/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteDto)))
                .andExpect(status().isInternalServerError());
        // Because Objects.requireNonNull throws NPE, default Spring behavior is 500
    }
}