package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.LoginRequest;
import com.zestindia.productservice.dto.request.RegisterRequest;
import com.zestindia.productservice.dto.request.TokenRefreshRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.JwtAuthResponse;
import com.zestindia.productservice.dto.response.TokenRefreshResponse;
import com.zestindia.productservice.entity.ERole;
import com.zestindia.productservice.entity.RefreshToken;
import com.zestindia.productservice.entity.Role;
import com.zestindia.productservice.entity.User;
import com.zestindia.productservice.exception.BadRequestException;
import com.zestindia.productservice.exception.TokenRefreshException;
import com.zestindia.productservice.repository.RefreshTokenRepository;
import com.zestindia.productservice.repository.RoleRepository;
import com.zestindia.productservice.repository.UserRepository;
import com.zestindia.productservice.security.JwtTokenProvider;
import com.zestindia.productservice.security.UserPrincipal;
import com.zestindia.productservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 3600000L);

        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                roleRepository,
                refreshTokenRepository,
                passwordEncoder,
                tokenProvider
        );
        ReflectionTestUtils.setField(authService, "refreshTokenDurationMs", 604800000L);

        userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1);

        sampleUser = new User("johndoe", "john@example.com", "encodedPassword");
        sampleUser.setId(1L);
        sampleUser.setRoles(Set.of(userRole));
    }

    @Test
    @DisplayName("Register User: should succeed when username and email are unique")
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("johndoe", "john@example.com", "Password@123", Set.of("user"));

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        ApiResponse<String> response = authService.registerUser(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("User registered successfully");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register User: should throw BadRequestException when username already taken")
    void testRegisterUser_UsernameTaken() {
        RegisterRequest request = new RegisterRequest("johndoe", "john@example.com", "Password@123", Set.of("user"));
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username 'johndoe' is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Authenticate User: should return access and refresh tokens")
    void testAuthenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest("johndoe", "Password@123");
        UserPrincipal userPrincipal = UserPrincipal.create(sampleUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("sample-refresh-token");
        when(refreshTokenRepository.findByUser(sampleUser)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        JwtAuthResponse response = authService.authenticateUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("johndoe");
        assertThat(response.getRoles()).contains("ROLE_USER");
    }

    @Test
    @DisplayName("Refresh Token: should rotate token and return new tokens")
    void testRefreshToken_Success() {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

        RefreshToken existingToken = new RefreshToken();
        existingToken.setUser(sampleUser);
        existingToken.setToken("valid-refresh-token");
        existingToken.setExpiryDate(Instant.now().plusSeconds(3600));
        existingToken.setRevoked(false);

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(existingToken);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotEqualTo("valid-refresh-token");
    }

    @Test
    @DisplayName("Refresh Token: should throw TokenRefreshException when token is expired")
    void testRefreshToken_Expired() {
        TokenRefreshRequest request = new TokenRefreshRequest("expired-token");

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(sampleUser);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600));
        expiredToken.setRevoked(false);

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }
}
