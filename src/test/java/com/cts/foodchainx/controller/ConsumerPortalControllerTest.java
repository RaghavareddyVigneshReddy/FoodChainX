package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.exception.BatchNotFoundException;
import com.cts.foodchainx.service.TraceabilityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ConsumerPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraceabilityService traceabilityService;

    @Test
    @DisplayName("GET /api/consumer/trace/{id} - Success")
    void getTraceRecord_Success() throws Exception {
        // FIX: Use Builder instead of 'new'
        TraceRecordResponseDto dto = TraceRecordResponseDto.builder()
                .status("IN_TRANSIT")
                // .batchId(101L) // Add other fields if needed
                .build();
        
        when(traceabilityService.getTraceabilityData(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/consumer/trace/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    @DisplayName("GET /api/consumer/qr/{id} - Success")
    void getQrCodePayload_Success() throws Exception {
        String payload = "FCX|101|CERTIFIED";
        when(traceabilityService.generateQrPayload(101L)).thenReturn(payload);

        mockMvc.perform(get("/api/consumer/qr/101"))
                .andExpect(status().isOk())
                .andExpect(content().string(payload));
    }

    @Test
    @DisplayName("GET /api/consumer/history/{id} - Success")
    void getBatchHistory_Success() throws Exception {
        // Use Builder to avoid "constructor undefined" error
        TraceRecordResponseDto traceRecord = TraceRecordResponseDto.builder().build();
        
        when(traceabilityService.getBatchHistory(101L)).thenReturn(List.of(traceRecord));

        mockMvc.perform(get("/api/consumer/history/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/consumer/trace/{id} - Not Found")
    void getTraceRecord_NotFound() throws Exception {
        when(traceabilityService.getTraceabilityData(999L))
                .thenThrow(new BatchNotFoundException(999L));

        mockMvc.perform(get("/api/consumer/trace/999"))
                .andExpect(status().isNotFound()); 
    }
}