package com.zestindia.productservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "JWT Authentication Response containing access and refresh tokens")
public class JwtAuthResponse {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token for rotating credentials", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type scheme", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "User email address", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Assigned security roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public JwtAuthResponse() {
    }

    public JwtAuthResponse(String accessToken, String refreshToken, Long id, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
