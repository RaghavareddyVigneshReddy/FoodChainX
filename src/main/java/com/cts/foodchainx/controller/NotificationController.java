package com.cts.foodchainx.controller;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor; // Fix S6813: Enables Constructor Injection
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // Fix Java(16778128): Null safety
import org.springframework.web.bind.annotation.*;

import com.cts.foodchainx.dto.notification.NotificationRequestDTO;
import com.cts.foodchainx.dto.notification.NotificationResponseDTO;
import com.cts.foodchainx.service.NotificationService;

@RestController
@RequestMapping("/foodchainx/notifications")
@RequiredArgsConstructor // Automatically creates constructor for final fields
public class NotificationController {

    // Final field ensures the service is injected during instantiation
    private final NotificationService notificationService;

    /**
     * US 1: Fetch alerts for a specific user.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(@RequestParam @NonNull Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    /**
     * US 2: Update status to 'Read'.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable @NonNull Long notificationId) {
        NotificationResponseDTO updated = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of(
            "message", "Notification updated to read successfully",
            "notificationId", updated.getNotificationId(),
            "status", updated.getStatus()
        ));
    }

    /**
     * US 3: Internal Creation using DTO.
     */
    @PostMapping("/internal")
    public ResponseEntity<NotificationResponseDTO> createInternal(@RequestBody @NonNull NotificationRequestDTO dto) {
        NotificationResponseDTO created = notificationService.createNotification(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * US 4: Deletion.
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable @NonNull Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }
}