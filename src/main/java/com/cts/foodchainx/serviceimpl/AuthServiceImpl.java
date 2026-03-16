

package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.exception.UserAlreadyExistsException;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.repository.UserRepository;
import com.cts.foodchainx.service.AuthService;
import com.cts.foodchainx.service.AuditLogService;
import com.cts.foodchainx.service.JwtService;
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
 * Implementation of {@link AuthService} providing business logic for IAM.
 * <p>
 * This service coordinates with Spring Security, the persistence layer, 
 * and the Audit module to ensure secure and logged user operations.
 * </p>
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
     * Registers a new user with duplicate validation.
     * <p>
     * Enforces uniqueness for both Email and Phone number. Upon success, 
     * the password is encrypted and an audit log is generated.
     * </p>
     * * @param req The registration payload.
     * @return {@link UserResponse} containing the generated User ID and basic info.
     * @throws UserAlreadyExistsException if email or phone is already in use.
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

        // 2. Build and Persist Entity
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
     * Authenticates user and checks account state.
     * <p>
     * Verifies credentials via the {@link AuthenticationManager}. Before issuing a token, 
     * it verifies that the user is not {@code SUSPENDED} or {@code INACTIVE}.
     * </p>
     * * @param req Credentials payload.
     * @return {@link TokenResponse} including Bearer token.
     * @throws LockedException if the account status is SUSPENDED.
     * @throws DisabledException if the account status is INACTIVE.
     */
    @Override
    public TokenResponse login(LoginRequest req) {
        // 1. Credentials Verification
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // 2. Fetch authenticated identity
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

        // 3. Domain-level Lifecycle Checks
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new LockedException("Your account has been suspended. Please contact support.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException("Your account is inactive.");
        }

        // 4. Token Generation and Logging
        String token = jwtService.generateToken(user);
        auditLogService.log(requireNonNull(user), "USER_LOGIN", "auth/login");

        return new TokenResponse(token, "Bearer", 86400); // 24-hour expiration
    }

    /**
     * Lists all system users.
     * * @return List of mapped UserResponse DTOs.
     */
    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Internal mapper to transform User entity to UserResponse DTO.
     * * @param user The source entity.
     * @return The target DTO.
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

