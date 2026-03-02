package com.cts.FoodChainX.dto.user;

import com.cts.FoodChainX.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank String name,
        @NotNull Role role,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String password // plaintext in request, will be hashed in service
) {}