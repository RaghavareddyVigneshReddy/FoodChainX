package com.cts.foodchainx.repository;

import com.cts.foodchainx.model.Role;
import com.cts.foodchainx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link User} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods for 
 * identity management and authentication.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their email, ignoring case sensitivity.
     * Primary use case: Spring Security authentication and JWT validation.
     * * @param email The user's email address.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks if a user already exists with the given email.
     * * @param email The email to check.
     * @return true if the email is already registered.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if a user already exists with the given phone number.
     * <p>
     * Used during registration to ensure the {@code unique} constraint 
     * on the phone column is not violated.
     * </p>
     * * @param phone The phone number to check.
     * @return true if the phone number is already in use.
     */
    boolean existsByPhone(String phone);

    /**
     * Finds all users assigned to a specific functional role.
     * * @param role The {@link Role} enum (e.g., FARMER, ADMIN).
     * @return A list of users matching the role.
     */
    List<User> findByRole(Role role);
}