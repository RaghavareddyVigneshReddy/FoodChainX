package com.cts.foodchainx.service; // Package set to service as requested

import com.cts.foodchainx.dto.audit.AuditLogResponse;
import com.cts.foodchainx.exception.AuditNotFoundException;
import com.cts.foodchainx.model.AuditLog;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.AuditLogRepository;
import com.cts.foodchainx.serviceimpl.AuditLogServiceImpl; // Explicit import required
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(100L)
                .email("tester@cts.com")
                .build();
    }

    @Test
    @DisplayName("log() - Should save AuditLog entry")
    void log_Success() {
        // Act
        auditLogService.log(testUser, "USER_LOGIN", "auth/login");

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        
        assertEquals("USER_LOGIN", captor.getValue().getAction());
        assertEquals(testUser, captor.getValue().getUser());
    }

    @Test
    @DisplayName("getLogsForUser() - Should return list of responses")
    void getLogsForUser_Success() {
        // Arrange
        AuditLog logEntry = AuditLog.builder()
                .auditId(1L)
                .user(testUser)
                .action("LOGIN")
                .resource("auth/login")
                .timestamp(Instant.now())
                .build();

        when(auditLogRepository.findByUser(testUser)).thenReturn(List.of(logEntry));

        // Act
        List<AuditLogResponse> results = auditLogService.getLogsForUser(testUser);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).userId());
    }

    @Test
    @DisplayName("getLogsForUser() - Should throw AuditNotFoundException")
    void getLogsForUser_NotFound() {
        // Arrange
        when(auditLogRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(AuditNotFoundException.class, () -> auditLogService.getLogsForUser(testUser));
    }

    @Test
    @DisplayName("getAllLogs() - Should return all logs")
    void getAllLogs_Success() {
        // Arrange
        when(auditLogRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<AuditLogResponse> results = auditLogService.getAllLogs();

        // Assert
        assertNotNull(results);
        verify(auditLogRepository).findAll();
    }
}