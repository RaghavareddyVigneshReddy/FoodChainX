package com.cts.foodchainx.controller;

import com.cts.foodchainx.config.JwtAuthenticationFilter;
import com.cts.foodchainx.config.SecurityConfig;
import com.cts.foodchainx.dto.user.UserStatusUpdateRequest;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.AuditLogService;
import com.cts.foodchainx.service.AuthService;
import com.cts.foodchainx.service.JwtService;
import com.cts.foodchainx.utils.SecurityTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserRepository userRepository;
    @MockitoBean private AuthService authService;
    @MockitoBean private AuditLogService auditLogService;

    @MockitoBean private JwtService jwtService;

    @Test
    @DisplayName("PATCH /status - Success for ADMIN")
    void updateStatus_AdminSuccess() throws Exception {
        // 1. Setup the custom actor principal
        SecurityTestUtils.setCustomUser(99L, "admin@cts.com", Role.ADMIN);

        UserStatusUpdateRequest req = new UserStatusUpdateRequest(UserStatus.SUSPENDED);
        User targetUser = User.builder().userId(1L).status(UserStatus.ACTIVE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        mockMvc.perform(patch("/api/admin/users/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - Forbidden for FARMER")
    void updateStatus_FarmerForbidden() throws Exception {
        // 1. Setup the context as a Farmer
        SecurityTestUtils.setCustomUser(100L, "farmer@cts.com", Role.FARMER);

        UserStatusUpdateRequest req = new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        mockMvc.perform(patch("/api/admin/users/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden()); // Security layer will now block correctly
    }
}