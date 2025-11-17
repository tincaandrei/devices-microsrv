package com.energy.userservice.service;

import com.energy.userservice.client.DeviceClient;
import com.energy.userservice.dto.UserProfileDTO;
import com.energy.userservice.dto.request.UserProfileRequest;
import com.energy.userservice.dto.response.UserDevicesResponse;
import com.energy.userservice.entity.UserProfile;
import com.energy.userservice.exception.DuplicateResourceException;
import com.energy.userservice.exception.ResourceNotFoundException;
import com.energy.userservice.mapper.UserProfileMapper;
import com.energy.userservice.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;
    private final DeviceClient deviceClient;

    public List<UserProfileDTO> findAll() {
        return repository.findAll().stream()
                .map(UserProfileMapper::toDto)
                .toList();
    }

    public UserProfileDTO findById(UUID id) {
        return UserProfileMapper.toDto(getOne(id));
    }

    @Transactional
    public UserProfileDTO create(UserProfileRequest request) {
        if (request.getId() == null) {
            throw new com.energy.userservice.exception.ApiException("User id is required");
        }
        if (repository.existsById(request.getId())) {
            throw new DuplicateResourceException("User already exists");
        }
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        UserProfile profile = new UserProfile();
        UserProfileMapper.update(profile, request);
        return UserProfileMapper.toDto(repository.save(profile));
    }

    @Transactional
    public UserProfileDTO update(UUID id, UserProfileRequest request) {
        UserProfile profile = getOne(id);
        if (!profile.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        UserProfileMapper.update(profile, request);
        return UserProfileMapper.toDto(repository.save(profile));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        repository.deleteById(id);
    }

    public UserDevicesResponse fetchUserDevices(UUID id, String authorizationHeader) {
        UserProfile profile = getOne(id);
        var devices = deviceClient.getDevicesByOwner(id, authorizationHeader);
        return UserDevicesResponse.builder()
                .user(UserProfileMapper.toDto(profile))
                .devices(devices)
                .build();
    }

    private UserProfile getOne(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
