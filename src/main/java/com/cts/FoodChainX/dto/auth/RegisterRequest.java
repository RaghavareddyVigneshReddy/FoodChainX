
package com.cts.FoodChainX.dto.auth;

import com.cts.FoodChainX.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String name,
        @NotNull Role role,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String password
) {}
