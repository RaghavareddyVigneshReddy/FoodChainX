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

import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /** GET /api/admin/users — list all users (ADMIN or REGULATOR) */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REGULATOR')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    /** PATCH /api/admin/users/{userId}/status — update user status (ADMIN only) */
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

    /** GET /api/admin/audit-logs — view system-wide audit logs (ADMIN or REGULATOR) */
    @SuppressWarnings("null")
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REGULATOR')")
    public List<AuditLogResponse> allAuditLogs() {
        return userRepository.findAll().stream()
                .filter(Objects::nonNull)
                // Fix: Cast 'u' to @NonNull to clear the remaining warning
                .flatMap(u -> auditLogService.getLogsForUser(u).stream())
                .toList();
    }
}