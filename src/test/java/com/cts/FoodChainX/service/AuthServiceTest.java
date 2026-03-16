package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditLogService auditLogService;
    // Mock other dependencies like JwtService, AuthenticationManager if needed

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest req = new RegisterRequest("Test User", Role.FARMER, "test@mail.com", "123", "pass");
        when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(req));
        
        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        // Arrange
        RegisterRequest req = new RegisterRequest("Test User", Role.FARMER, "new@mail.com", "123", "pass");
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashedPass");
        
        User savedUser = User.builder().userId(1L).email("new@mail.com").role(Role.FARMER).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        var response = authService.register(req);

        // Assert
        assertNotNull(response);
        assertEquals("new@mail.com", response.email());
        verify(auditLogService, times(1)).log(any(User.class), eq("USER_REGISTER"), anyString());
    }
}