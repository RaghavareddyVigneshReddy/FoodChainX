package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.auth.LoginRequest;
import com.cts.foodchainx.dto.auth.RegisterRequest;
import com.cts.foodchainx.dto.auth.TokenResponse;
import com.cts.foodchainx.dto.user.UserResponse;
import com.cts.foodchainx.enums.Role;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.service.AuthService;
import com.cts.foodchainx.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController using MockitoBean.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService; // Mocked to satisfy the security context

    @Test
    @DisplayName("POST /api/auth/register - Success returns 201 Created")
    void register_Success() throws Exception {
        // Arrange - Note the order: name, role, email, phone, password
        RegisterRequest request = new RegisterRequest(
                "Vignesh", 
                Role.ADMIN, 
                "vignesh@cts.com", 
                "9988776655", 
                "securePass"
        );

        UserResponse response = new UserResponse(
                1L, 
                "Vignesh", 
                Role.ADMIN, 
                "vignesh@cts.com", 
                "9988776655", 
                UserStatus.ACTIVE
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("vignesh@cts.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success returns 200 OK")
    void login_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("vignesh@cts.com", "securePass");
        TokenResponse response = new TokenResponse("mock-jwt-token", "Bearer", 86400);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Returns 400 on Validation Failure")
    void register_ValidationFailure() throws Exception {
        // Act & Assert (Empty JSON to trigger @Validated constraints)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}