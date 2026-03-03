package com.cts.FoodChainX.controller;

import java.util.List; // Verify this import
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.FoodChainX.dto.notification.NotificationRequestDTO;
import com.cts.FoodChainX.model.Notification;
import com.cts.FoodChainX.service.NotificationService;

@RestController
@RequestMapping("/foodchainx/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // US 1: Fetch using Long userId to match the User entity
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    // US 2: Update status
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable int notificationId) {
        Notification updated = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of(
            "message", "Notification updated to read successfully",
            "notificationId", updated.getNotificationId(),
            "status", updated.getStatus()
        ));
    }

    // US 3: Internal Creation using DTO
    @PostMapping("/internal")
    public ResponseEntity<Notification> createInternal(@RequestBody NotificationRequestDTO dto) {
        Notification notification = Notification.builder()
                .entityId(dto.getEntityId())
                .message(dto.getMessage())
                .category(dto.getCategory())
                .build();
        return new ResponseEntity<>(notificationService.createNotification(dto.getUserId(), notification), HttpStatus.CREATED);
    }

    // US 4: Deletion
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable int notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }
}
