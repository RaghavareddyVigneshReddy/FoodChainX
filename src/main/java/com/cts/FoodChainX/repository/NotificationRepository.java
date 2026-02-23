package com.cts.FoodChainX.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.FoodChainX.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(int userId);
    //It allows you to filter notifications based on whether the user has seen them or not
    List<Notification> findByUserIdAndStatus(int userId, String status);
}
