package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.model.UserStatus;
import com.cts.foodchainx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new UserAlreadyExistsException("Email " + req.email() + " is already taken.");
        }

        User user = User.builder()
                .name(req.name())
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .phone(req.phone())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(requireNonNull(user));
        
        // System log for new registration
        auditLogService.log(user, "USER_REGISTER", "users/" + user.getUserId());

        return mapToResponse(user);
    }

    public TokenResponse login(LoginRequest req) {
        // 1. Authenticate via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // 2. Fetch user to generate token
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        String token = jwtService.generateToken(user);
        
        auditLogService.log(requireNonNull(user), "USER_LOGIN", "auth/login");

        return new TokenResponse(token, "Bearer", 86400); // 24h expiry
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getStatus()
        );
    }
}