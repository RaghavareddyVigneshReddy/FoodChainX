package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import com.cts.foodchainx.service.NotificationService;
import com.cts.foodchainx.service.JwtService;
import com.cts.foodchainx.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses Security filters to avoid 401/403 errors during unit tests
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    // These two mocks fix the 'Application Failed to Start' error
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = NotificationResponseDTO.builder()
                .notificationId(1L)
                .entityId(101L)
                .message("Compliance Alert: Batch 101 Failed")
                .category("URGENT")
                .status("UNREAD")
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getNotifications_ShouldReturnList() throws Exception {
        Mockito.when(notificationService.getNotificationsForUser(anyLong()))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/foodchainx/notifications")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Compliance Alert: Batch 101 Failed"))
                .andExpect(jsonPath("$[0].status").value("UNREAD"));
    }

    @Test
    void markAsRead_ShouldReturnSuccessMap() throws Exception {
        responseDTO.setStatus("READ");
        Mockito.when(notificationService.markAsRead(anyLong()))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/foodchainx/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.message").value("Notification updated to read successfully"));
    }

    @Test
    void createInternal_ShouldReturnCreated() throws Exception {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setEntityId(101L);
        requestDTO.setMessage("New Notification");
        requestDTO.setCategory("GENERAL");

        Mockito.when(notificationService.createNotification(any(NotificationRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/foodchainx/notifications/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value(1L));
    }

    @Test
    void deleteNotification_ShouldReturnOk() throws Exception {
        Mockito.doNothing().when(notificationService).deleteNotification(anyLong());

        mockMvc.perform(delete("/foodchainx/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }
}