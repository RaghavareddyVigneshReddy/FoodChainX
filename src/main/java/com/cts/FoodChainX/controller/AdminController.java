package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.audit.AuditLogResponse;
import com.cts.FoodChainX.dto.user.UserResponse;
import com.cts.FoodChainX.dto.user.UserStatusUpdateRequest;
import com.cts.FoodChainX.model.User;
//import com.cts.FoodChainX.model.UserStatus;
import com.cts.FoodChainX.repository.UserRepository;
import com.cts.FoodChainX.service.AuditLogService;
import com.cts.FoodChainX.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public UserResponse updateStatus(@PathVariable Long userId,
                                     @Valid @RequestBody UserStatusUpdateRequest req,
                                     @AuthenticationPrincipal User actor) {
        // Reuse update method or write a thin status-only logic
        var user = userRepository.findById(userId).orElseThrow();
        user.setStatus(req.status());
        user = userRepository.save(user);
        auditLogService.log(actor, "UPDATE_USER_STATUS", "users/" + user.getUserId());
        return new UserResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getStatus()
        );
    }

    /** GET /api/admin/audit-logs — view system-wide audit logs (ADMIN or REGULATOR) */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REGULATOR')")
    public List<AuditLogResponse> allAuditLogs() {
        // If you want filters (date range, action), extend service/repo
        return userRepository.findAll().stream()
                .flatMap(u -> auditLogService.getLogsForUser(u).stream())
                .toList();
    }
}
