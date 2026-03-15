package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static java.util.Objects.requireNonNull;

/**
 * Service handling Authentication and Authorization logic.
 * <p>
 * This class manages the user lifecycle from registration to login,
 * ensuring secure password handling and JWT generation.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    /**
     * Registers a new user in the system.
     * <p>
     * Performs duplicate email and unique phone number checks, hashes the password, 
     * and creates an audit log entry for the new registration.
     * </p>
     *
     * @param req The registration details provided by the client.
     * @return {@link UserResponse} representing the newly created user.
     * @throws UserAlreadyExistsException if the email or phone number is already registered.
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        // 1. Check for duplicate email
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new UserAlreadyExistsException("Email " + req.email() + " is already taken.");
        }
        // 2. Check for duplicate phone number if provided
        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
            throw new UserAlreadyExistsException("Phone number " + req.phone() + " is already in use.");
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
        
        auditLogService.log(user, "USER_REGISTER", "users/" + user.getUserId());

        return mapToResponse(user);
    }

    /**
     * Authenticates a user based on email and password.
     * <p>
     * Utilizes Spring Security's {@link AuthenticationManager} to verify credentials.
     * Upon success, generates a JWT for the user and logs the login event.
     * </p>
     *
     * @param req The login credentials.
     * @return {@link TokenResponse} containing the JWT and expiry details.
     */
    public TokenResponse login(LoginRequest req) {
        // 1. Authenticate via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // 2. Fetch user to generate token
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        // 3. Status Check: Block restricted users before issuing a token
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new LockedException(
                "Your account has been suspended. Please contact support."
            );
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException(
                "Your account is inactive."
            );
        }

        String token = jwtService.generateToken(user);
        
        auditLogService.log(requireNonNull(user), "USER_LOGIN", "auth/login");

        return new TokenResponse(token, "Bearer", 86400); // 24h expiry
    }

    /**
     * Lists all registered users in the platform.
     *
     * @return A list of all users as {@link UserResponse} DTOs.
     */
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Maps a {@link User} entity to its corresponding {@link UserResponse} DTO.
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(), user.getName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getStatus()
        );
    }
}