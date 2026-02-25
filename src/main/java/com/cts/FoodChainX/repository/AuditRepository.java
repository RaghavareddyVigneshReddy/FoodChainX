package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer> {
    
    // Find all logs for a specific user
    List<Audit> findByUserId(Integer userId);
    
    // Find logs filtered by action type (e.g., "LOGIN", "DELETE")
    List<Audit> findByAction(String action);
}