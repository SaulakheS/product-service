package com.zestindia.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productservice.dto.request.LoginRequest;
import com.zestindia.productservice.dto.request.RegisterRequest;
import com.zestindia.productservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /api/v1/auth/register: Successfully registers user")
    void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@example.com", "Password@123", Set.of("user"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: Fails when email is invalid format")
    void testRegisterUser_InvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("user2", "invalid-email-format", "Password@123", Set.of("user"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: Successfully logs in with seeded admin user")
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "Admin@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: Returns 401 Unauthorized for bad credentials")
    void testLogin_BadCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "WrongPassword@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
