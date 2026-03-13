package com.cts.foodchainx.dto.user;


import com.cts.foodchainx.model.Role;
import com.cts.foodchainx.model.UserStatus;

public record UserUpdateRequest(
        String name,
        Role role,
        String phone,
        UserStatus status
) {}