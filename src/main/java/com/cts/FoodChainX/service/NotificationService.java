package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.notification.NotificationRequestDTO;
import com.cts.FoodChainX.dto.notification.NotificationResponseDTO;
import com.cts.FoodChainX.exception.NotificationNotFoundException;
import com.cts.FoodChainX.model.Notification;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.NotificationRepository;
import com.cts.FoodChainX.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves notifications for a user and maps them to Response DTOs.
     */
    public List<NotificationResponseDTO> getNotificationsForUser(Long userId) {
        log.info("User {} is retrieving their alerts.", userId);
        List<Notification> list = repository.findByUserUserIdOrderByCreatedDateDesc(userId);
        
        if (list.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for user " + userId);
        }
        
        return list.stream()
                   .map(this::mapToResponseDTO)
                   .collect(Collectors.toList());
    }

    /**
     * Updates notification status to 'Read' and returns the updated DTO.
     */
    public NotificationResponseDTO markAsRead(int notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification ID " + notificationId + " not found"));
        
        n.setStatus("Read");
        Notification saved = repository.save(n);
        return mapToResponseDTO(saved);
    }

    /**
     * Creates a notification using a Request DTO and establishes User relation.
     */
    public NotificationResponseDTO createNotification(NotificationRequestDTO dto) {
    // 1. Fetch User based on the ID inside the DTO
    User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

    // 2. Build the Notification entity
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setEntityId(dto.getEntityId());
    notification.setMessage(dto.getMessage());
    notification.setCategory(dto.getCategory());
    notification.setStatus("Unread");
    notification.setCreatedDate(java.time.LocalDateTime.now());

    // 3. Save and map to Response DTO
    Notification saved = repository.save(notification);
    return mapToResponseDTO(saved);
}

    /**
     * Deletes a notification by ID.
     */
    public void deleteNotification(int notificationId) {
        if (!repository.existsById(notificationId)) {
            throw new NotificationNotFoundException("Notification ID " + notificationId + " not found");
        }
        repository.deleteById(notificationId);
    }

    /**
     * Helper method to map Entity to Response DTO.
     */
    private NotificationResponseDTO mapToResponseDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .entityId(n.getEntityId())
                .message(n.getMessage())
                .category(n.getCategory())
                .status(n.getStatus())
                .createdDate(n.getCreatedDate())
                .build();
    }
}