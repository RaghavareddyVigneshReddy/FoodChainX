package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    
    // Find all logs for a specific user
    List<AuditLog> findByUserId(Integer userId);
    
    // Find logs filtered by action type (e.g., "LOGIN", "DELETE")
    List<AuditLog> findByAction(String action);
}