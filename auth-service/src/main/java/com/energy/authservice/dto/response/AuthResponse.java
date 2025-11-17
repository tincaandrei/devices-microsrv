package com.energy.authservice.dto.response;

import com.energy.authservice.entity.Role;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    UUID credentialId;
    String username;
    Role role;
    String token;
    Instant expiresAt;
}
