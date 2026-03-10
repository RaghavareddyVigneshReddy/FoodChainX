package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.notification.NotificationRequestDTO;
import com.cts.FoodChainX.dto.notification.NotificationResponseDTO;
import com.cts.FoodChainX.exception.NotificationNotFoundException;
import com.cts.FoodChainX.model.Notification;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.NotificationRepository;
import com.cts.FoodChainX.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User mockUser;
    private Notification mockNotification;
    private NotificationRequestDTO mockRequestDTO;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setName("Jagan");

        mockNotification = new Notification();
        mockNotification.setNotificationId(101);
        mockNotification.setMessage("Test Alert");
        mockNotification.setStatus("Unread");
        mockNotification.setUser(mockUser);

        // Define a DTO for input testing
        mockRequestDTO = NotificationRequestDTO.builder()
                .userId(1L)
                .entityId(501)
                .message("Test Alert")
                .category("Compliance")
                .build();
    }

    @Test
    void testGetNotificationsForUser_Success() {
        when(notificationRepository.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(List.of(mockNotification));

        // Change return type to NotificationResponseDTO list
        List<NotificationResponseDTO> result = notificationService.getNotificationsForUser(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Alert", result.get(0).getMessage());
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(101)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        // Change return type to NotificationResponseDTO
        NotificationResponseDTO result = notificationService.markAsRead(101);

        assertEquals("Read", result.getStatus());
    }

    @Test
    void testCreateNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        // Pass the DTO instead of the entity
        NotificationResponseDTO created = notificationService.createNotification(mockRequestDTO);

        assertNotNull(created);
        assertEquals(101, created.getNotificationId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
    
    // Deletion tests remain largely the same as they don't involve DTO returns
}