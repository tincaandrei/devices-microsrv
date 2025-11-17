package com.energy.deviceservice.repository;

import com.energy.deviceservice.entity.Device;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findAllByOwnerId(UUID ownerId);

    List<Device> findAllByOwnerIdIsNull();
}
