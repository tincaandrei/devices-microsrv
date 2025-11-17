package com.energy.authservice.service;

import com.energy.authservice.dto.CredentialDTO;
import com.energy.authservice.dto.request.RegisterRequest;
import com.energy.authservice.entity.Credential;
import com.energy.authservice.exception.DuplicateResourceException;
import com.energy.authservice.exception.ResourceNotFoundException;
import com.energy.authservice.mapper.CredentialMapper;
import com.energy.authservice.repository.CredentialRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CredentialDTO register(RegisterRequest request) {
        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        Credential credential = new Credential();
        credential.setUsername(request.getUsername());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setEmail(request.getEmail());
        credential.setRole(request.getRole() == null ? com.energy.authservice.entity.Role.CLIENT : request.getRole());
        Credential saved = credentialRepository.save(credential);
        return CredentialMapper.toDto(saved);
    }

    public Credential getCredentialByUsername(String username) {
        return credentialRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Credential getById(UUID id) {
        return credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
