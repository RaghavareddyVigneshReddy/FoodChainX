package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Custom query to find a user by their unique email
    Optional<User> findByEmail(String email);
    
    // Custom query to find users by their role (e.g., "ADMIN", "USER")
    java.util.List<User> findByRole(String role);
}