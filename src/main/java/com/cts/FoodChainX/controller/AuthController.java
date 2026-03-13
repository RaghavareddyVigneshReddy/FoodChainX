package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Public Authentication Controller.
 * <p>
 * Handles user onboarding (registration) and identity verification (login). 
 * These endpoints are excluded from standard JWT filtering to allow 
 * access to non-authenticated users.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the FoodChainX platform.
     * <p>
     * Validates input data and creates a user record with an initial status.
     * Returns HTTP 201 (Created) upon successful account creation.
     * </p>
     * * @param req The registration details (Email, Password, Name, Role).
     * @return ResponseEntity containing the newly created {@link UserResponse} with status 201.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Validated @RequestBody RegisterRequest req) {
        UserResponse response = authService.register(req);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and generates a JWT.
     * <p>
     * Verifies credentials and returns a Bearer token if successful.
     * Returns HTTP 200 (OK) upon successful authentication.
     * </p>
     * * @param req The login credentials (Email, Password).
     * @return ResponseEntity containing the {@link TokenResponse} (JWT) with status 200.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest req) {
        TokenResponse response = authService.login(req);
        return ResponseEntity.ok(response);
    }
}