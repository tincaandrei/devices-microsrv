package com.energy.authservice.repository;

import com.energy.authservice.entity.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByUsername(String username);
    boolean existsByUsername(String username);
}
