package com.cts.foodchainx.controller;

import com.cts.foodchainx.enums.ComplianceResult;
import com.cts.foodchainx.enums.ComplianceType;
import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.service.ComplianceRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
class ComplianceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ComplianceRecordService complianceRecordService;

    @Test
    @DisplayName("POST /api/compliance/records - Success when REGULATOR")
    @WithMockUser(roles = "REGULATOR")
    @SuppressWarnings("null")
    void createRecord_Success() throws Exception {
        ComplianceRecord complianceRecord = ComplianceRecord.builder()
                .entityId(500L)
                .type(ComplianceType.FARMER)
                .result(ComplianceResult.PASSED)
                .build();

        when(complianceRecordService.createComplianceRecord(any(ComplianceRecord.class)))
                .thenReturn(complianceRecord);

        String jsonContent = objectMapper.writeValueAsString(complianceRecord);

        mockMvc.perform(post("/api/compliance/records")
                        .with(csrf()) // Required if CSRF is enabled in SecurityConfig
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityId").value(500))
                .andExpect(jsonPath("$.result").value("PASSED"));
    }

    @Test
    @DisplayName("GET /api/compliance/history/{id} - Success for authenticated users")
    @WithMockUser(roles = "FARMER")
    void getHistory_Success() throws Exception {
        ComplianceRecord complianceRecord = ComplianceRecord.builder().entityId(500L).build();
        when(complianceRecordService.getHistoryByEntity(500L)).thenReturn(List.of(complianceRecord));

        mockMvc.perform(get("/api/compliance/history/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityId").value(500));
    }

    @Test
    @DisplayName("GET /api/compliance/failed - Forbidden for FARMER")
    @WithMockUser(roles = "FARMER")
    void getFailed_Forbidden() throws Exception {
        mockMvc.perform(get("/api/compliance/failed"))
                .andExpect(status().isForbidden());
    }
}