package com.energy.deviceservice.service;

import com.energy.deviceservice.dto.DeviceDTO;
import com.energy.deviceservice.dto.request.DeviceRequest;
import com.energy.deviceservice.entity.Device;
import com.energy.deviceservice.exception.ResourceNotFoundException;
import com.energy.deviceservice.mapper.DeviceMapper;
import com.energy.deviceservice.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    public List<DeviceDTO> findAll() {
        return repository.findAll().stream()
                .map(DeviceMapper::toDto)
                .toList();
    }

    public DeviceDTO findById(UUID id) {
        return DeviceMapper.toDto(getOne(id));
    }

    public List<DeviceDTO> findByOwner(UUID ownerId) {
        return repository.findAllByOwnerId(ownerId).stream()
                .map(DeviceMapper::toDto)
                .toList();
    }

    public List<DeviceDTO> findAvailable() {
        return repository.findAllByOwnerIdIsNull().stream()
                .map(DeviceMapper::toDto)
                .toList();
    }

    @Transactional
    public DeviceDTO create(DeviceRequest request) {
        Device device = new Device();
        DeviceMapper.update(device, request);
        return DeviceMapper.toDto(repository.save(device));
    }

    @Transactional
    public DeviceDTO update(UUID id, DeviceRequest request) {
        Device device = getOne(id);
        DeviceMapper.update(device, request);
        return DeviceMapper.toDto(repository.save(device));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found");
        }
        repository.deleteById(id);
    }

    @Transactional
    public DeviceDTO assign(UUID id, UUID ownerId) {
        Device device = getOne(id);
        device.setOwnerId(ownerId);
        return DeviceMapper.toDto(repository.save(device));
    }

    @Transactional
    public DeviceDTO unassign(UUID id) {
        Device device = getOne(id);
        device.setOwnerId(null);
        return DeviceMapper.toDto(repository.save(device));
    }

    private Device getOne(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
    }
}
