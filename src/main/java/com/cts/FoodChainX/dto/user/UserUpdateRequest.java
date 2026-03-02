package com.cts.FoodChainX.dto.user;


import com.cts.FoodChainX.model.Role;
import com.cts.FoodChainX.model.UserStatus;

public record UserUpdateRequest(
        String name,
        Role role,
        String phone,
        UserStatus status
) {}