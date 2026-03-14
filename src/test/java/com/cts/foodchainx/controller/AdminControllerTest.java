package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.model.Role;
import com.cts.foodchainx.model.UserStatus;
import com.cts.foodchainx.service.AuthService;
import com.cts.foodchainx.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = "file:.env")
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    @DisplayName("GET /api/admin/users - Success when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void listUsers_Success() throws Exception {
        // Arrange
        UserResponse user = new UserResponse(1L, "Test", Role.FARMER, "test@mail.com", "123", UserStatus.ACTIVE);
        when(authService.listUsers()).thenReturn(List.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@mail.com"))
                .andExpect(jsonPath("$[0].role").value("FARMER"));
    }

    @Test
    @DisplayName("GET /api/admin/users - Forbidden when FARMER")
    @WithMockUser(roles = "FARMER")
    void listUsers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/audit-logs - Success when REGULATOR")
    @WithMockUser(roles = "REGULATOR")
    void auditLogs_RegulatorSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isOk());
    }
}