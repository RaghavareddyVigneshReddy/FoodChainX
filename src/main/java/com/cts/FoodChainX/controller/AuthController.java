package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.auth.LoginRequest;
import com.cts.FoodChainX.dto.auth.RegisterRequest;
import com.cts.FoodChainX.dto.auth.TokenResponse;
import com.cts.FoodChainX.dto.user.UserResponse;
import com.cts.FoodChainX.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Validated @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}