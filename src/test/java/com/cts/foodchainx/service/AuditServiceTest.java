package com.cts.foodchainx.service;

import com.cts.foodchainx.enums.AuditStatus;
import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.repository.AuditRepository;
import com.cts.foodchainx.serviceimpl.AuditServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private Audit testAudit;

    @BeforeEach
    void setUp() {
        testAudit = new Audit();
        testAudit.setAuditId(101L);
        testAudit.setScope("Annual Supply Chain Review");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should initialize and save a new audit record")
    void createAudit_Success() {
        // Arrange
        when(auditRepository.save(any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Audit result = auditService.createAudit(testAudit);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getDate());
        assertEquals(AuditStatus.OPEN, result.getStatus());
        verify(auditRepository, times(1)).save(testAudit);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should transition audit status to CLOSED when found")
    void closeAudit_Success() {
        // Arrange
        testAudit.setStatus(AuditStatus.OPEN);
        when(auditRepository.findById(101L)).thenReturn(Optional.of(testAudit));
        when(auditRepository.save(any(Audit.class))).thenReturn(testAudit);

        // Act
        Audit result = auditService.closeAudit(101L);

        // Assert
        assertEquals(AuditStatus.CLOSED, result.getStatus());
        verify(auditRepository).findById(101L);
        verify(auditRepository).save(testAudit);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should throw exception when closing non-existent audit")
    void closeAudit_NotFound() {
        // Arrange
        when(auditRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auditService.closeAudit(999L);
        });

        assertEquals("Audit record not found for ID: 999", exception.getMessage());
        verify(auditRepository, never()).save(any());
    }
}