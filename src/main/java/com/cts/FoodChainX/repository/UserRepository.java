package com.cts.FoodChainX.repository;

import com.cts.FoodChainX.model.Role;
import com.cts.FoodChainX.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by SecurityConfig and JwtAuthenticationFilter
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Use Role enum for type safety
    List<User> findByRole(Role role);
}