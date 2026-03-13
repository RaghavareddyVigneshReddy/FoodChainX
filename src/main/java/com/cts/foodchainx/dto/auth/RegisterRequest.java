<<<<<<< HEAD

=======
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
package com.cts.foodchainx.dto.auth;

import com.cts.foodchainx.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

<<<<<<< HEAD
=======
/**
 * Request payload for registering a new user in the platform.
 * * @param name     The full name of the user or organization.
 * @param role     The system role assigned to the user (e.g., FARMER, DISTRIBUTOR).
 * @param email    The unique email address used for login and notifications.
 * @param phone    The contact phone number (optional).
 * @param password The raw password to be hashed before storage.
 */
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
public record RegisterRequest(
        @NotBlank String name,
        @NotNull Role role,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String password
<<<<<<< HEAD
) {}
=======
) {}
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
