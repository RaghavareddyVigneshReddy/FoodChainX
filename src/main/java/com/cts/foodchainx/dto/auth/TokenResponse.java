package com.cts.foodchainx.dto.auth;

<<<<<<< HEAD
=======
/**
 * Response payload containing the JWT authentication token.
 * * @param accessToken      The generated JSON Web Token (JWT) to be used in Bearer Auth.
 * @param tokenType        The type of token, typically "Bearer".
 * @param expiresInSeconds The duration in seconds until the token becomes invalid.
 */
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}