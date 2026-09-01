package com.zestindia.productservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload containing newly rotated JWT access and refresh tokens")
public class TokenRefreshResponse {

    @Schema(description = "New JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Rotated Refresh Token", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    private String refreshToken;

    @Schema(description = "Token type scheme", example = "Bearer")
    private String tokenType = "Bearer";

    public TokenRefreshResponse() {
    }

    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
