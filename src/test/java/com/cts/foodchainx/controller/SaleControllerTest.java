
package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.sale.SaleRequestDTO;
import com.cts.foodchainx.model.Sale;
import com.cts.foodchainx.serviceimpl.SaleServiceImpl;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleServiceImpl saleServiceImpl;

    @Test
    @DisplayName("POST /api/retail/sales - Success when Consumer creates a sale")
    @WithMockUser(roles = "CONSUMER")
    void createSale_Success() throws Exception {
        // 1. Setup Request DTO
        SaleRequestDTO requestDto = new SaleRequestDTO();
        requestDto.setInventoryId(101L);
        requestDto.setConsumerId(202L);
        requestDto.setQuantity(5L);
        requestDto.setPrice(150.0);

        // 2. Setup Mocked Response Entity
        Sale savedSale = new Sale();
       // savedSale.setId(1L); // Assuming Sale has an ID after saving
        savedSale.setInventoryId(101L);
        savedSale.setConsumerId(202L);
        savedSale.setQuantity(5L);
        savedSale.setPrice(150.0);

        when(saleServiceImpl.createSale(any(Sale.class))).thenReturn(savedSale);

        // 3. Perform Request and Assert
        mockMvc.perform(post("/api/retail/sales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()) // Your controller doesn't specify @ResponseStatus, so it defaults to 200 OK
                .andExpect(jsonPath("$.inventoryId").value(101))
                .andExpect(jsonPath("$.consumerId").value(202))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(150.0));
    }

    @Test
    @DisplayName("POST /api/retail/sales - Unauthorized without user")
    void createSale_Unauthorized() throws Exception {
        SaleRequestDTO requestDto = new SaleRequestDTO();

        mockMvc.perform(post("/api/retail/sales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}