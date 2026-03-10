package com.cts.FoodChainX.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.FoodChainX.dto.notification.NotificationRequestDTO;
import com.cts.FoodChainX.dto.notification.NotificationResponseDTO;
import com.cts.FoodChainX.service.NotificationService;

@RestController
@RequestMapping("/foodchainx/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * US 1: Fetch alerts for a specific user.
     * Returning NotificationResponseDTO prevents the infinite recursion of the User object.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    /**
     * US 2: Update status to 'Read'.
     */
   @PutMapping("/{notificationId}/read")
public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable int notificationId) {
    NotificationResponseDTO updated = notificationService.markAsRead(notificationId);
    return ResponseEntity.ok(Map.of(
        "message", "Notification updated to read successfully",
        "notificationId", updated.getNotificationId(),
        "status", updated.getStatus()
    ));
}

    /**
     * US 3: Internal Creation using DTO.
     * Note: We no longer build the 'Notification' object here; 
     * the Service does that using the DTO we send it.
     */
    @PostMapping("/internal")
    public ResponseEntity<NotificationResponseDTO> createInternal(@RequestBody NotificationRequestDTO dto) {
        NotificationResponseDTO created = notificationService.createNotification(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * US 4: Deletion.
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable int notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }
}