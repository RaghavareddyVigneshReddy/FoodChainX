package com.cts.foodchainx.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user authentication.
 * * @param email    The user's registered email address (must be a valid email format).
 * @param password The user's plain-text password to be verified against the stored hash.
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}