package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.LoginRequest;
import com.zestindia.productservice.dto.request.RegisterRequest;
import com.zestindia.productservice.dto.request.TokenRefreshRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.JwtAuthResponse;
import com.zestindia.productservice.dto.response.TokenRefreshResponse;

public interface AuthService {

    ApiResponse<String> registerUser(RegisterRequest registerRequest);

    JwtAuthResponse authenticateUser(LoginRequest loginRequest);

    TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest);

    ApiResponse<String> logout(String refreshTokenStr);
}
