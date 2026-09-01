package com.zestindia.productservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token request payload for token rotation")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Existing valid refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    public TokenRefreshRequest() {
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
