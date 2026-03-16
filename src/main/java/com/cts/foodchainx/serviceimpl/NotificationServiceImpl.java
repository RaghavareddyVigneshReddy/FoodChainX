package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import com.cts.foodchainx.enums.NotificationStatus;
import com.cts.foodchainx.exception.NotificationNotFoundException;
import com.cts.foodchainx.model.Notification;
import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.NotificationRepository;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationResponseDTO> getNotificationsForUser(@NonNull Long userId) {
        log.info("User {} is retrieving their alerts.", userId);
        List<Notification> list = repository.findByUserUserIdOrderByCreatedDateDesc(userId);
        
        if (list.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for user " + userId);
        }
        
        return list.stream()
                   .map(this::mapToResponseDTO)
                   .toList(); 
    }

    @Override
    public NotificationResponseDTO markAsRead(@NonNull Long notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification ID " + notificationId + " not found"));
        
        n.setStatus(NotificationStatus.READ);
        Notification saved = repository.save(n);
        return mapToResponseDTO(saved);
    }

    @SuppressWarnings("null")
    @Override
    public NotificationResponseDTO createNotification(@NonNull NotificationRequestDTO dto) {
        User user = userRepository.findById(Objects.requireNonNull(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .entityId(dto.getEntityId())
                .message(dto.getMessage())
                .category(dto.getCategory())
                .status(NotificationStatus.UNREAD)
                .createdDate(java.time.LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);
        return mapToResponseDTO(saved);

        
    }

    @Override
    @Auditable(action = "DELETE_NOTIFICATION", resource = "NOTIFICATION")
    public void deleteNotification(@NonNull Long notificationId) {
        if (!repository.existsById(notificationId)) {
            throw new NotificationNotFoundException("Notification ID " + notificationId + " not found");
        }
        repository.deleteById(notificationId);
    }

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