package com.cts.foodchainx.dto.user;


import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;

public record UserUpdateRequest(
        String name,
        Role role,
        String phone,
        UserStatus status
) {}