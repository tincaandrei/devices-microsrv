package com.energy.deviceservice.controller;

import com.energy.deviceservice.dto.DeviceDTO;
import com.energy.deviceservice.dto.request.DeviceRequest;
import com.energy.deviceservice.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public List<DeviceDTO> findAll() {

        return deviceService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceDTO create(
            @Valid @RequestBody DeviceRequest request,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        return deviceService.create(request);
    }

    @GetMapping("/{id}")
    public DeviceDTO findOne(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        DeviceDTO device = deviceService.findById(id);
        ensureOwnerOrAdmin(role, userId, device.getOwnerId());
        return device;
    }

    @PutMapping("/{id}")
    public DeviceDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceRequest request,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        return deviceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role
    ) {
        ensureAdmin(role);
        deviceService.delete(id);
    }

    @PostMapping("/{id}/assign/{userId}")
    public DeviceDTO assign(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String callerUserId
    ) {
        String roleValue = requireRole(role);
        UUID callerId = requireUserId(callerUserId);

        if (!isAdmin(roleValue) && !callerId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only assign yourself");
        }

        return deviceService.assign(id, userId);
    }

    @PostMapping("/{id}/unassign")
    public DeviceDTO unassign(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String callerUserId
    ) {
        String roleValue = requireRole(role);
        UUID callerId = requireUserId(callerUserId);
        DeviceDTO device = deviceService.findById(id);

        if (!isAdmin(roleValue) && (device.getOwnerId() == null || !device.getOwnerId().equals(callerId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only unassign your own devices");
        }

        return deviceService.unassign(id);
    }


    @GetMapping("/owner/{ownerId}")
    public List<DeviceDTO> byOwner(
            @PathVariable UUID ownerId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        ensureOwnerOrAdmin(role, userId, ownerId);
        return deviceService.findByOwner(ownerId);
    }

    @GetMapping("/me")
    public List<DeviceDTO> myDevices(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UUID requesterId = requireUserId(userId);
        return deviceService.findByOwner(requesterId);
    }

    @GetMapping("/available")
    public List<DeviceDTO> availableDevices() {
        return deviceService.findAvailable();
    }

    private void ensureAdmin(String roleHeader) {
        if (!isAdmin(requireRole(roleHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    private void ensureOwnerOrAdmin(String roleHeader, String userIdHeader, UUID ownerId) {
        String role = requireRole(roleHeader);
        UUID requesterId = requireUserId(userIdHeader);
        if (ownerId == null && !isAdmin(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device is not assigned");
        }
        if (!isAdmin(role) && ownerId != null && !ownerId.equals(requesterId)) {
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

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }
}
