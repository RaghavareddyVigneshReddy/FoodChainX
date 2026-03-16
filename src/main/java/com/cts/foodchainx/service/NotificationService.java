package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import org.springframework.lang.NonNull;
import java.util.List;

public interface NotificationService {
    List<NotificationResponseDTO> getNotificationsForUser(@NonNull Long userId);
    NotificationResponseDTO markAsRead(@NonNull Long notificationId);
    NotificationResponseDTO createNotification(@NonNull NotificationRequestDTO dto);
    void deleteNotification(@NonNull Long notificationId);
}