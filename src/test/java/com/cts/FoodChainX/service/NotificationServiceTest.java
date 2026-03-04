package com.cts.FoodChainX.service;

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
    }

    // --- US 1: Retrieval Tests ---
    @Test
    void testGetNotificationsForUser_Success() {
        when(notificationRepository.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(List.of(mockNotification));

        List<Notification> result = notificationService.getNotificationsForUser(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserUserIdOrderByCreatedDateDesc(1L);
    }

    @Test
    void testGetNotificationsForUser_NotFound() {
        when(notificationRepository.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(new ArrayList<>());

        assertThrows(NotificationNotFoundException.class, () -> 
            notificationService.getNotificationsForUser(1L));
    }

    // --- US 2: Mark as Read Tests ---
    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(101)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        Notification result = notificationService.markAsRead(101);

        assertEquals("Read", result.getStatus());
        verify(notificationRepository, times(1)).save(mockNotification);
    }

    // --- US 3: Creation Tests ---
    @Test
    void testCreateNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        Notification created = notificationService.createNotification(1L, mockNotification);

        assertNotNull(created);
        assertEquals(mockUser, created.getUser());
        verify(notificationRepository, times(1)).save(mockNotification);
    }

    // --- US 4: Deletion Tests ---
    @Test
    void testDeleteNotification_Success() {
        when(notificationRepository.existsById(101)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(101);

        assertDoesNotThrow(() -> notificationService.deleteNotification(101));
        verify(notificationRepository, times(1)).deleteById(101);
    }

    @Test
    void testDeleteNotification_NotFound() {
        when(notificationRepository.existsById(999)).thenReturn(false);

        assertThrows(NotificationNotFoundException.class, () -> 
            notificationService.deleteNotification(999));
    }
}