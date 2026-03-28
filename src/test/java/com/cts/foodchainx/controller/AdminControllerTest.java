package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.user.UserStatusUpdateRequest;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Ensures it uses your H2/Test DB
@Transactional // Rolls back the database changes after each test
class AdminControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("INTEGRATION: PATCH /status - Verify DB update and Audit Log")
    @WithMockUser(username = "admin@cts.com", roles = {"ADMIN"})
    void updateStatus_IntegrationSuccess() throws Exception {
        // 1. Arrange: Actually save a user to the REAL (Test) Database
        User user = User.builder()
                .name("Target User")
                .email("target@user.com")
                .role(Role.FARMER)
                .status(UserStatus.ACTIVE)
                .passwordHash("encoded_pass")
                .build();
        User savedUser = userRepository.save(user);

        UserStatusUpdateRequest req = new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        // 2. Act: Call the API
        mockMvc.perform(patch("/api/admin/users/" + savedUser.getUserId() + "/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 3. Assert: Verify the database was actually updated
        User updatedUser = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertEquals(UserStatus.SUSPENDED, updatedUser.getStatus());

        // 4. Assert: Verify an Audit Log entry was created
        long auditCount = auditLogRepository.count();
        assertEquals(1, auditCount, "An audit log entry should have been created for this admin action");
    }
}