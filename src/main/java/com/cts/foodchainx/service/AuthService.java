package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import java.util.List;

/**
 * Interface for Authentication and Identity Management services.
 * <p>
 * Defines the contract for user onboarding, secure authentication, 
 * and user information retrieval within the FoodChainX ecosystem.
 * </p>
 */
public interface AuthService {

    /**
     * Handles the logic for creating a new user account.
     * * @param req The registration details provided by the user.
     * @return A DTO representing the successfully registered user.
     */
    UserResponse register(RegisterRequest req);

    /**
     * Validates credentials and generates an access token.
     * * @param req The email and password credentials.
     * @return A response containing the JWT and its metadata.
     */
    TokenResponse login(LoginRequest req);

    /**
     * Retrieves a summary of all registered users.
     * * @return A list of user profile responses.
     */
    List<UserResponse> listUsers();
}