package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.report.ReportResponseDto;
import com.cts.foodchainx.service.ReportService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    @DisplayName("GET /api/reports/performance - Success for ADMIN")
    @WithMockUser(roles = "ADMIN")
    @SuppressWarnings("null")
    void getPerformanceDashboard_Admin_Success() throws Exception {
        // Arrange
        ReportResponseDto mockResponse = ReportResponseDto.builder()
                .scope("FARMER")
                .metrics(Map.of("harvestQualityPassRate", "95.00%"))
                .build();

        when(reportService.generateScopedPerformance("FARMER")).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/reports/performance")
                        .param("scope", "FARMER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("FARMER"))
                .andExpect(jsonPath("$.metrics.harvestQualityPassRate").value("95.00%"));
    }

    @Test
    @DisplayName("GET /api/reports/performance - Success for REGULATOR")
    @WithMockUser(roles = "REGULATOR")
    void getPerformanceDashboard_Regulator_Success() throws Exception {
        when(reportService.generateScopedPerformance(anyString()))
                .thenReturn(ReportResponseDto.builder().scope("DISTRIBUTOR").build());

        mockMvc.perform(get("/api/reports/performance")
                        .param("scope", "DISTRIBUTOR"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/performance - Forbidden for FARMER")
    @WithMockUser(roles = "FARMER")
    void getPerformanceDashboard_Farmer_Forbidden() throws Exception {
        // Act & Assert: Should return 403 because @PreAuthorize excludes FARMER
        mockMvc.perform(get("/api/reports/performance")
                        .param("scope", "FARMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reports/performance - Unauthorized for Guest")
    void getPerformanceDashboard_Guest_Unauthorized() throws Exception {
        // Act & Assert: No user context should return 401
        mockMvc.perform(get("/api/reports/performance"))
                .andExpect(status().isUnauthorized());
    }
}