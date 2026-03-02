package com.cts.FoodChainX.dto.user;

import com.cts.FoodChainX.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(@NotNull UserStatus status) { }