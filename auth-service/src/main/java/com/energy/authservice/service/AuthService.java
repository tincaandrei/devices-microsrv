package com.energy.authservice.service;

import com.energy.authservice.client.UserProfileClient;
import com.energy.authservice.dto.CredentialDTO;
import com.energy.authservice.dto.request.LoginRequest;
import com.energy.authservice.dto.request.RegisterRequest;
import com.energy.authservice.dto.response.AuthResponse;
import com.energy.authservice.dto.response.TokenValidationResponse;
import com.energy.authservice.entity.Credential;
import com.energy.authservice.entity.Role;
import com.energy.authservice.exception.InvalidCredentialsException;
import com.energy.authservice.security.JwtService;
import com.energy.authservice.security.JwtToken;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CredentialService credentialService;
    private final JwtService jwtService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final UserProfileClient userProfileClient;

    public AuthResponse register(RegisterRequest request) {
        CredentialDTO dto = credentialService.register(request);
        Credential credential = credentialService.getCredentialByUsername(dto.getUsername());

        // Best-effort automatic creation of corresponding user profile in User Service.
        try {
            userProfileClient.createUserProfile(credential.getId(), credential.getUsername(), credential.getEmail());
        } catch (Exception ex) {
            log.warn("Failed to auto-create user profile for credential {}", credential.getId(), ex);
        }

        JwtToken token = jwtService.generateToken(credential);
        return buildAuthResponse(credential, token);
    }

    public AuthResponse login(LoginRequest request) {
        Credential credential = credentialService.getCredentialByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new InvalidCredentialsException();
        }
        JwtToken token = jwtService.generateToken(credential);
        return buildAuthResponse(credential, token);
    }

    public TokenValidationResponse validateToken(String token) {
        boolean valid = jwtService.isTokenValid(token);
        if (!valid) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }
        String roleClaim = jwtService.extractRole(token);
        Role role = null;
        if (roleClaim != null) {
            String normalized = roleClaim.startsWith("ROLE_") ? roleClaim.substring(5) : roleClaim;
            if ("ADMIN".equalsIgnoreCase(normalized)) {
                role = Role.ADMIN;
            } else if ("CLIENT".equalsIgnoreCase(normalized) || "USER".equalsIgnoreCase(normalized)) {
                role = Role.CLIENT;
            }
        }
        return TokenValidationResponse.builder()
                .valid(true)
                .credentialId(jwtService.extractUserId(token))
                .role(role)
                .username(jwtService.extractUsername(token))
                .expiresAt(jwtService.extractExpiration(token))
                .build();
    }

    private AuthResponse buildAuthResponse(Credential credential, JwtToken token) {
        return AuthResponse.builder()
                .credentialId(credential.getId())
                .username(credential.getUsername())
                .role(credential.getRole())
                .token(token.value())
                .expiresAt(token.expiresAt())
                .build();
    }
}
