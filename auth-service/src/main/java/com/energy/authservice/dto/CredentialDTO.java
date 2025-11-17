package com.energy.authservice.dto;

import com.energy.authservice.entity.Role;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CredentialDTO {
    UUID id;
    String username;
    String email;
    Role role;
}
