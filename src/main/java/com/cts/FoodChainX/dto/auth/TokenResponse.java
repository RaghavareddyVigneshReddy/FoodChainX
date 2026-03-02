package com.cts.FoodChainX.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}