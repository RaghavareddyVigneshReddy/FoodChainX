package com.cts.foodchainx.dto.user;

import com.cts.foodchainx.model.Role;
import com.cts.foodchainx.model.UserStatus;

public record UserResponse(
        Long userId,
        String name,
        Role role,
        String email,
        String phone,
        UserStatus status
) {}