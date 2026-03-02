package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.AuditLog;
import com.cts.FoodChainX.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find all logs for a specific user by entity
    List<AuditLog> findByUser(User user);

    // Find all logs for a specific user by their userId via property path
    List<AuditLog> findByUser_UserId(Long userId);

    // Find logs filtered by action type (e.g., "LOGIN", "DELETE")
    List<AuditLog> findByAction(String action);

    // Optional: time-range filter if needed
    List<AuditLog> findByTimestampBetween(Instant from, Instant to);
}