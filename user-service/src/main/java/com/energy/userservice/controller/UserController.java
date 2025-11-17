package com.energy.userservice.controller;

import com.energy.userservice.dto.UserProfileDTO;
import com.energy.userservice.dto.request.UserProfileRequest;
import com.energy.userservice.dto.response.UserDevicesResponse;
import com.energy.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping
    public List<UserProfileDTO> findAll(
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        return userProfileService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileDTO create(
            @Valid @RequestBody UserProfileRequest request,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        return userProfileService.create(request);
    }

    @GetMapping("/{id}")
    public UserProfileDTO findOne(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        ensureSelfOrAdmin(role, userId, id);
        return userProfileService.findById(id);
    }

    @PutMapping("/{id}")
    public UserProfileDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileRequest request,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        if (!id.equals(request.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload id must match path id");
        }
        return userProfileService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        userProfileService.delete(id);
    }

    @GetMapping("/{id}/devices")
    public UserDevicesResponse devices(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        ensureSelfOrAdmin(role, userId, id);
        return userProfileService.fetchUserDevices(id, requireAuthorization(authorization));
    }

    @GetMapping("/me")
    public UserProfileDTO me(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UUID requesterId = requireUserId(userId);
        return userProfileService.findById(requesterId);
    }

    @PutMapping("/me")
    public UserProfileDTO updateMe(
            @Valid @RequestBody UserProfileRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UUID requesterId = requireUserId(userId);
        if (!requesterId.equals(request.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload id must match authenticated user id");
        }
        return userProfileService.update(requesterId, request);
    }

    @GetMapping("/me/devices")
    public UserDevicesResponse myDevices(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        UUID requesterId = requireUserId(userId);
        return userProfileService.fetchUserDevices(requesterId, requireAuthorization(authorization));
    }

    private void ensureAdmin(String roleHeader) {
        if (!isAdmin(requireRole(roleHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    private void ensureSelfOrAdmin(String roleHeader, String userIdHeader, UUID requestedUserId) {
        String role = requireRole(roleHeader);
        UUID requesterId = requireUserId(userIdHeader);
        if (!isAdmin(role) && !requesterId.equals(requestedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
    }

    private String requireRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Role header");
        }
        return roleHeader;
    }

    private UUID requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid X-User-Id header");
        }
    }

    private String requireAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        return authorizationHeader;
    }

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }
}
