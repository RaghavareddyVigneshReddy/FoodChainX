package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.audit.AuditLogResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.dto.user.UserStatusUpdateRequest;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.AuditLogService;
import com.cts.foodchainx.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Administrative controller for the IAM module.
 * <p>
 * Provides high-privileged endpoints for user management and system-wide 
 * audit visibility. Access is restricted primarily to users with {@code ADMIN} 
 * or {@code REGULATOR} roles.
 * </p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "2. Admin Management", description = "High-privileged tasks for ADMIN and REGULATOR roles")
@SecurityRequirement(name = "Bearer Authentication") // Applies the lock icon to all endpoints in this class
public class AdminController {

    private final AuthService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Retrieves a list of all registered users in the system.
     * * @return A list of {@link UserResponse} DTOs.
     * @throws org.springframework.security.access.AccessDeniedException if user lacks ADMIN or REGULATOR roles.
     * 
     */
    @Operation(
        summary = "List All Users", 
        description = "Returns a complete list of users. Accessible by ADMIN and REGULATOR."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REGULATOR')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    /**
     * Updates the status (e.g., ACTIVE, SUSPENDED) of a specific user.
     * <p>
     * This operation is strictly limited to {@code ADMIN} users. Every status change 
     * is manually logged to the audit service to track administrative actions.
     * </p>
     * * @param userId The ID of the user to update.
     * @param req The request body containing the new status.
     * @param actor The currently authenticated administrator performing the action.
     * @return The updated {@link UserResponse}.
     */
    @Operation(
        summary = "Update User Status", 
        description = "Change a user's status (ACTIVE/SUSPENDED). Strictly limited to ADMIN users."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated and logged"),
        @ApiResponse(responseCode = "404", description = "User ID not found"),
        @ApiResponse(responseCode = "403", description = "Only ADMIN can perform this action")
    })
    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateStatus(@PathVariable @NonNull Long userId,
                                     @Valid @RequestBody @NonNull UserStatusUpdateRequest req,
                                     @AuthenticationPrincipal User actor) {
        
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        
        user.setStatus(req.status());
        user = userRepository.save(user);

        // Fail-fast check for security context
        Objects.requireNonNull(actor, "Actor must not be null");
        auditLogService.log(actor, "UPDATE_USER_STATUS", "users/" + user.getUserId());

        return new UserResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getStatus()
        );
    }

    /**
     * Fetches system-wide audit logs.
     * <p>
     * Aggregates logs across all users to provide a comprehensive view of system activity.
     * This is used by regulators for compliance checks.
     * </p>
     * * @return A list of all {@link AuditLogResponse} entries.
     */
    @Operation(
        summary = "Fetch Global Audit Logs", 
        description = "Aggregates all system activity logs. Used primarily for regulatory compliance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication token missing or invalid")
    })
    @SuppressWarnings("null")
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REGULATOR')")
    public List<AuditLogResponse> allAuditLogs() {
        return userRepository.findAll().stream()
                .filter(Objects::nonNull)
                .flatMap(u -> auditLogService.getLogsForUser(u).stream())
                .toList();
    }
}