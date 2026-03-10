package com.cts.FoodChainX.service;

import com.cts.FoodChainX.exception.NotificationNotFoundException;
import com.cts.FoodChainX.model.Notification;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.NotificationRepository;
import com.cts.FoodChainX.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        log.info("User {} is retrieving their alerts.", userId); //
        List<Notification> list = repository.findByUserUserIdOrderByCreatedDateDesc(userId);
        // if (list.isEmpty()) {
        //     throw new NotificationNotFoundException("No notifications found for user " + userId);
        // }
        return list;
    }

    public Notification markAsRead(int notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification ID " + notificationId + " not found"));
        n.setStatus("Read"); //
        return repository.save(n);
    }

    public Notification createNotification(Long userId, Notification notification) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        notification.setUser(user); // Establishing relation
        log.info("Notification created for User {} regarding Entity {}", userId, notification.getEntityId());
        return repository.save(notification);
    }

    public void deleteNotification(int notificationId) {
        if (!repository.existsById(notificationId)) {
            throw new NotificationNotFoundException("Notification ID " + notificationId + " not found");
        }
        repository.deleteById(notificationId); //
    }
}