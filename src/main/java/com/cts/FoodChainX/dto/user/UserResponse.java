package com.cts.FoodChainX.dto.user;

import com.cts.FoodChainX.model.Role;
import com.cts.FoodChainX.model.UserStatus;

public record UserResponse(
        Long userId,
        String name,
        Role role,
        String email,
        String phone,
        UserStatus status
) {}