package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.exception.AccountStatusException;
import com.cts.foodchainx.exception.InvalidCredentialsException;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.AuthService;
import com.cts.foodchainx.service.AuditLogService;
import com.cts.foodchainx.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of AuthService providing business logic for Identity & Access Management (IAM).
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    /**
     * Registers a new user with uniqueness validation for email and phone.
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest req) {
        // 1. Uniqueness Validation
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new UserAlreadyExistsException("Email " + req.email() + " is already taken.");
        }

        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
            throw new UserAlreadyExistsException("Phone number " + req.phone() + " is already in use.");
        }

        // 2. Build and Persist Entity (Mapping password to passwordHash)
        User user = User.builder()
                .name(req.name())
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .phone(req.phone())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(requireNonNull(user));
        
        // 3. System Auditing
        auditLogService.log(user, "USER_REGISTER", "users/" + user.getUserId());

        return mapToResponse(user);
    }

    /**
     * Authenticates user and verifies account lifecycle status.
     */
    @Override
    @Transactional
    public TokenResponse login(LoginRequest req) {
        try {
            // 1. Credentials Verification via Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            // Re-throw as custom InvalidCredentialsException for GlobalExceptionHandler (401)
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        // 2. Fetch User Identity
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new InvalidCredentialsException("User record not found."));

        // 3. Domain-level Status Checks (Caught as 403 Forbidden by Handler)
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountStatusException("Your account has been suspended. Please contact support.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AccountStatusException("Your account is currently inactive.");
        }

        // 4. Token Generation and Logging
        String token = jwtService.generateToken(user);
        auditLogService.log(user, "USER_LOGIN", "auth/login");

        return new TokenResponse(token, "Bearer", 86400); // 24-hour expiration
    }

    /**
     * Retrieves all system users mapped to DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Internal mapper: Entity -> UserResponse Record.
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(), 
                user.getName(), 
                user.getRole(),
                user.getEmail(), 
                user.getPhone(), 
                user.getStatus()
        );
    }
}