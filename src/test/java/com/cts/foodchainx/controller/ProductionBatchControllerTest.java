package com.cts.foodchainx.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.service.ProductionBatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class ProductionBatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductionBatchService batchService;

    @InjectMocks
    private ProductionBatchController batchController;

    private ObjectMapper objectMapper;
    private BatchResponseDto mockResponse;
    private BatchRequestDto mockRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // matches your BatchRequestDto (assuming it has these 4 fields)
        mockRequest = new BatchRequestDto(1L, "Organic Wheat", 500.0, LocalDate.now()); 
        
        // Matches your NEW BatchResponseDto (only batchId and qualityStatus)
        mockResponse = new BatchResponseDto(101L, "PENDING");
    }

    @SuppressWarnings("null")
    @Test
    void createBatch_ShouldReturnCreated() throws Exception {
        when(batchService.createBatch(any(BatchRequestDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/production/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.batchId").value(101L))
            .andExpect(jsonPath("$.qualityStatus").value("PENDING"));
    }

    @Test
    void getBatchById_ShouldReturnBatch() throws Exception {
        when(batchService.getBatchById(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/production/101"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(101L))
            .andExpect(jsonPath("$.qualityStatus").value("PENDING"));
    }

    @Test
    void getBatchesByFarm_ShouldReturnList() throws Exception {
        when(batchService.getBatchesByFarm(anyLong())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/production/farm/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].batchId").value(101L))
            .andExpect(jsonPath("$[0].qualityStatus").value("PENDING"));
    }

    @Test
    void deleteBatch_ShouldReturnSuccessMessage() throws Exception {
        String msg = "Batch deleted successfully";
        when(batchService.deleteBatch(anyLong())).thenReturn(msg);

        mockMvc.perform(delete("/api/production/101"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(msg));
    }

    @Test
    void getBatchFullDetails_ShouldReturnDetails() throws Exception {
        // Full details usually include MORE than the standard response
        BatchDetailResponseDto detailResponse = new BatchDetailResponseDto();
        detailResponse.setBatchId(101L);
        detailResponse.setCropType("Organic Wheat");
        detailResponse.setFarmName("Green Valley");
        
        when(batchService.getBatchDetail(anyLong())).thenReturn(detailResponse);

        mockMvc.perform(get("/api/production/101/details"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(101L))
            .andExpect(jsonPath("$.cropType").value("Organic Wheat"));
    }
}
