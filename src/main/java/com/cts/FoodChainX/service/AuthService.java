
package com.cts.FoodChainX.service;

import com.cts.FoodChainX.dto.auth.LoginRequest;
import com.cts.FoodChainX.dto.auth.RegisterRequest;
import com.cts.FoodChainX.dto.auth.TokenResponse;
import com.cts.FoodChainX.dto.user.UserResponse;
import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.model.UserStatus;
import com.cts.FoodChainX.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder; // <-- REQUIRED for register()

    public TokenResponse login(LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        authenticationManager.authenticate(authToken); // throws if invalid

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);

        auditLogService.log(user, "LOGIN", "auth/login");
        // If you have the actual expiry configured in application properties, you can expose it via JwtService.
        return new TokenResponse(token, "Bearer", 60 * 60);
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(req.name())
                .role(req.role())
                .email(req.email())
                .phone(req.phone())
                .status(UserStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(req.password())) // hash securely
                .build();

        user = userRepository.save(user);
        auditLogService.log(user, "REGISTER", "auth/register");

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