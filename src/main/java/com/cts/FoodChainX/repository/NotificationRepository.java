package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Navigate from Notification -> User -> userId
    List<Notification> findByUserUserIdOrderByCreatedDateDesc(Long userId);

    List<Notification> findByUserUserIdAndStatus(Long userId, String status);
}