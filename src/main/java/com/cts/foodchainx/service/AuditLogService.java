package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.audit.AuditLogResponse;
import com.cts.foodchainx.model.AuditLog;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(@NonNull User user,@NonNull String action, @NonNull String resource) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .timestamp(Instant.now())
                .build();
                
        auditLogRepository.save(java.util.Objects.requireNonNull(entry));
    }

    public List<AuditLogResponse> getLogsForUser(@NonNull User user) {
        return auditLogRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    private AuditLogResponse mapToDto(AuditLog log) {
        return new AuditLogResponse(
                log.getAuditId(),
                log.getUser().getUserId(),
                log.getAction(),
                log.getResource(),
                log.getTimestamp()
        );
    }
}