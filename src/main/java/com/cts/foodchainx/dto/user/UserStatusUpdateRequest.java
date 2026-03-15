package com.cts.foodchainx.dto.user;

import com.cts.foodchainx.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(@NotNull UserStatus status) { }