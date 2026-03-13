package com.cts.foodchainx.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}