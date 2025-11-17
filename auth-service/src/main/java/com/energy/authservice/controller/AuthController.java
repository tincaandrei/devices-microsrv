package com.energy.authservice.controller;

import com.energy.authservice.dto.CredentialDTO;
import com.energy.authservice.dto.request.LoginRequest;
import com.energy.authservice.dto.request.RegisterRequest;
import com.energy.authservice.dto.response.AuthResponse;
import com.energy.authservice.dto.response.TokenValidationResponse;
import com.energy.authservice.mapper.CredentialMapper;
import com.energy.authservice.security.CredentialUserDetails;
import com.energy.authservice.service.AuthService;
import com.energy.authservice.service.CredentialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CredentialService credentialService;

    public AuthController(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public ResponseEntity<CredentialDTO> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CredentialUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var credential = credentialService.getCredentialByUsername(userDetails.getUsername());
        return ResponseEntity.ok(CredentialMapper.toDto(credential));
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String header) {
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : header;
        if (token == null || token.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header missing");
        }
        TokenValidationResponse response = authService.validateToken(token);
        HttpStatus status = response.isValid() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (response.isValid()) {
            if (response.getUsername() != null) {
                headers.add("X-User", response.getUsername());
            }
            if (response.getCredentialId() != null) {
                headers.add("X-User-Id", response.getCredentialId().toString());
            }
            if (response.getRole() != null) {
                String roleHeader = response.getRole() == com.energy.authservice.entity.Role.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
                headers.add("X-Role", roleHeader);
            }
        }

        return ResponseEntity.status(status)
                .headers(headers)
                .body(response);
    }
}
