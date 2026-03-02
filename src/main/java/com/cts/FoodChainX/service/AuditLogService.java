package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.AuditLog;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.dto.audit.AuditLogResponse;
import com.cts.FoodChainX.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, String action, String resource) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getLogsForUser(User user) {
        return auditLogRepository.findByUser(user).stream()
                .map(l -> new AuditLogResponse(
                        l.getAuditId(),
                        l.getUser().getUserId(),
                        l.getAction(),
                        l.getResource(),
                        l.getTimestamp()
                ))
                .toList();
    }
}