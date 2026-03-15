package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import com.cts.foodchainx.enums.NotificationStatus;
import com.cts.foodchainx.exception.NotificationNotFoundException;
import com.cts.foodchainx.model.Notification;
import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.NotificationRepository;
import com.cts.foodchainx.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Added for Constructor Injection
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull; // Added for Null Safety
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor // Fix S6813: Replaces @Autowired field injection
public class NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    /**
     * Retrieves notifications for a user and maps them to Response DTOs.
     */
    public List<NotificationResponseDTO> getNotificationsForUser(@NonNull Long userId) {
        log.info("User {} is retrieving their alerts.", userId);
        List<Notification> list = repository.findByUserUserIdOrderByCreatedDateDesc(userId);
        
        if (list.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for user " + userId);
        }
        
        return list.stream()
                   .map(this::mapToResponseDTO)
                   .toList(); // Fix S6204: Modern Java 16+ Stream collection
    }

    /**
     * Updates notification status to 'Read' and returns the updated DTO.
     */
    public NotificationResponseDTO markAsRead(@NonNull Long notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification ID " + notificationId + " not found"));
        
        n.setStatus(NotificationStatus.READ);
        Notification saved = repository.save(n);
        return mapToResponseDTO(saved);
    }

    /**
     * Creates a notification using a Request DTO and establishes User relation.
     */
    public NotificationResponseDTO createNotification(@NonNull NotificationRequestDTO dto) {
        // 1. Fetch User based on the ID inside the DTO (with null check)
        User user = userRepository.findById(Objects.requireNonNull(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        // 2. Build the Notification entity
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEntityId(dto.getEntityId());
        notification.setMessage(dto.getMessage());
        notification.setCategory(dto.getCategory());
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedDate(java.time.LocalDateTime.now());

        // 3. Save and map to Response DTO
        Notification saved = repository.save(notification);
        return mapToResponseDTO(saved);
    }

    /**
     * Deletes a notification by ID.
     */
    @Auditable(action = "DELETE_NOTIFICATION", resource = "NOTIFICATION")
    public void deleteNotification(@NonNull Long notificationId) {
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
                .status(n.getStatus().name())
                .createdDate(n.getCreatedDate())
                .build();
    }
}