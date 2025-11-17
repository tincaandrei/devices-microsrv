package com.energy.authservice.mapper;

import com.energy.authservice.dto.CredentialDTO;
import com.energy.authservice.entity.Credential;

public final class CredentialMapper {

    private CredentialMapper() {
    }

    public static CredentialDTO toDto(Credential credential) {
        return CredentialDTO.builder()
                .id(credential.getId())
                .username(credential.getUsername())
                .email(credential.getEmail())
                .role(credential.getRole())
                .build();
    }
}
