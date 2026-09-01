package com.zestindia.productservice.controller;

import com.zestindia.productservice.dto.request.LoginRequest;
import com.zestindia.productservice.dto.request.RegisterRequest;
import com.zestindia.productservice.dto.request.TokenRefreshRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.JwtAuthResponse;
import com.zestindia.productservice.dto.response.TokenRefreshResponse;
import com.zestindia.productservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Management", description = "Endpoints for user registration, authentication, token rotation, and logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user with standard USER role (or ADMIN if specified).")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        ApiResponse<String> response = authService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT & Refresh tokens", description = "Validates user credentials and returns short-lived JWT access token and rotating refresh token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthResponse authResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful!", authResponse));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate refresh token & issue new access token", description = "Rotates refresh token by invalidating previous token and issuing new access token + new refresh token.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully!", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user refresh token", description = "Invalidates the provided refresh token preventing any further token rotations.")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody TokenRefreshRequest request) {
        ApiResponse<String> response = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
