package com.zestindia.productservice.service.impl;

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
import com.zestindia.productservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public ApiResponse<String> registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Error: Username '" + registerRequest.getUsername() + "' is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Error: Email address '" + registerRequest.getEmail() + "' is already in use!");
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase().trim()) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
                        roles.add(adminRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        log.info("User successfully registered: {}", user.getUsername());
        return ApiResponse.success("User registered successfully!");
    }

    @Override
    @Transactional
    public JwtAuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String jwt = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = createOrUpdateRefreshToken(userPrincipal.getId());

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User authenticated successfully: {}", userPrincipal.getUsername());

        return new JwtAuthResponse(
                jwt,
                refreshToken.getToken(),
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                roles
        );
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database."));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException(requestRefreshToken, "Refresh token has been revoked.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(requestRefreshToken, "Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateTokenFromUserPrincipal(userPrincipal);

        // Refresh Token Rotation: Invalidate previous and issue fresh refresh token
        String newRefreshTokenStr = UUID.randomUUID().toString();
        refreshToken.setToken(newRefreshTokenStr);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshTokenRepository.save(refreshToken);

        log.info("Rotated refresh token for user: {}", user.getUsername());

        return new TokenRefreshResponse(newAccessToken, newRefreshTokenStr);
    }

    @Override
    @Transactional
    public ApiResponse<String> logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(refreshToken -> {
                refreshToken.setRevoked(true);
                refreshTokenRepository.save(refreshToken);
                log.info("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
            });
        }
        return ApiResponse.success("Log out successful!");
    }

    private RefreshToken createOrUpdateRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(RefreshToken::new);

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }
}
