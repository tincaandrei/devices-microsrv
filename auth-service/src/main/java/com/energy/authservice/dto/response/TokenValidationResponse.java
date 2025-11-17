package com.energy.authservice.dto.response;

import com.energy.authservice.entity.Role;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenValidationResponse {
    boolean valid;
    String username;
    Role role;
    UUID credentialId;
    Instant expiresAt;
}
