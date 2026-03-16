package com.cts.foodchainx.dto.user;

import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;

public record UserResponse(
        Long userId,
        String name,
        Role role,
        String email,
        String phone,
        UserStatus status
) {}