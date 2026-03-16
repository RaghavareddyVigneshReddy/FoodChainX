package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.user.UserStatusUpdateRequest;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.AuditLogService;
import com.cts.foodchainx.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserRepository userRepository;
    @MockitoBean private AuthService authService;
    @MockitoBean private AuditLogService auditLogService;

    @Test
    @DisplayName("PATCH /status - Success for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_AdminSuccess() throws Exception {
        UserStatusUpdateRequest req = new UserStatusUpdateRequest(UserStatus.SUSPENDED);
        User mockUser = User.builder().userId(1L).status(UserStatus.ACTIVE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        mockMvc.perform(patch("/api/admin/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - Forbidden for FARMER")
    @WithMockUser(roles = "FARMER")
    void updateStatus_FarmerForbidden() throws Exception {
        UserStatusUpdateRequest req = new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        mockMvc.perform(patch("/api/admin/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}