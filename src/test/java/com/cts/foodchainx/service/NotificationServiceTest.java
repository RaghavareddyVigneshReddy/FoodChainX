package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import com.cts.foodchainx.model.Notification;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.NotificationRepository;
import com.cts.foodchainx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // FIXED: Using Long literal (L)
        mockNotification.setNotificationId(101L); 
        mockNotification.setMessage("Test Alert");
        mockNotification.setStatus("Unread");
        mockNotification.setUser(mockUser);
        // Ensure entityId is also Long if your model was updated
        mockNotification.setEntityId(501L); 

        mockRequestDTO = NotificationRequestDTO.builder()
                .userId(1L)
                .entityId(501L) // FIXED: Long literal
                .message("Test Alert")
                .category("Compliance")
                .build();
    }

    @Test
    void testGetNotificationsForUser_Success() {
        when(notificationRepository.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(List.of(mockNotification));

        List<NotificationResponseDTO> result = notificationService.getNotificationsForUser(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Alert", result.get(0).getMessage());
    }

    @Test
    void testMarkAsRead_Success() {
        // FIXED: 101L
        when(notificationRepository.findById(101L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        NotificationResponseDTO result = notificationService.markAsRead(101L);

        assertEquals("Read", result.getStatus());
    }

    @Test
    void testCreateNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        NotificationResponseDTO created = notificationService.createNotification(mockRequestDTO);

        assertNotNull(created);
        // FIXED: Expecting Long
        assertEquals(101L, created.getNotificationId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification_Success() {
        // FIXED: 101L and ensuring existence check is mocked
        when(notificationRepository.existsById(101L)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(101L);

        assertDoesNotThrow(() -> notificationService.deleteNotification(101L));
        verify(notificationRepository, times(1)).deleteById(101L);
    }
}