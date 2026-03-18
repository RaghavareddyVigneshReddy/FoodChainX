package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.exception.AccountStatusException;
import com.cts.foodchainx.exception.InvalidCredentialsException;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.serviceimpl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .userId(1L)
                .name("Test User")
                .email("test@foodchainx.com")
                .passwordHash("encodedPass") // Fixed: matched the field name in User entity
                .role(Role.FARMER)
                .status(UserStatus.ACTIVE)
                .phone("9876543210")
                .build();
    }

    @Test
    @DisplayName("register() - Should throw UserAlreadyExistsException if email taken")
    void register_EmailExists_ThrowsException() {
        RegisterRequest req = new RegisterRequest("Test", Role.FARMER, "test@foodchainx.com", "123", "pass");
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("login() - Should throw InvalidCredentialsException for unknown email")
    void login_UnknownEmail_ThrowsException() {
        LoginRequest req = new LoginRequest("unknown@email.com", "pass");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("login() - Should throw AccountStatusException if user is SUSPENDED")
    void login_SuspendedUser_ThrowsException() {
        activeUser.setStatus(UserStatus.SUSPENDED);
        LoginRequest req = new LoginRequest("test@foodchainx.com", "pass");
        
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));

        assertThrows(AccountStatusException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("login() - Should wrap BadCredentialsException in custom InvalidCredentialsException")
    void login_WrongPassword_ThrowsException() {
        LoginRequest req = new LoginRequest("test@foodchainx.com", "wrongPass");
        
        // Simulating Spring Security Authentication Failure
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("login() - Success should return token and log audit")
    void login_Success() {
        LoginRequest req = new LoginRequest("test@foodchainx.com", "pass");
        
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(activeUser)).thenReturn("mocked-jwt-token");

        TokenResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.accessToken());
        verify(auditLogService).log(eq(activeUser), eq("USER_LOGIN"), anyString());
    }
}