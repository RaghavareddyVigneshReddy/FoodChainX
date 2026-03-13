package com.cts.foodchainx.dto.auth;

/**
 * Response payload containing the JWT authentication token.
 * * @param accessToken      The generated JSON Web Token (JWT) to be used in Bearer Auth.
 * @param tokenType        The type of token, typically "Bearer".
 * @param expiresInSeconds The duration in seconds until the token becomes invalid.
 */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}