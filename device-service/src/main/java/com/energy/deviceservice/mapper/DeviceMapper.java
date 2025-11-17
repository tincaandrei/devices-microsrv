package com.energy.deviceservice.mapper;

import com.energy.deviceservice.dto.DeviceDTO;
import com.energy.deviceservice.dto.request.DeviceRequest;
import com.energy.deviceservice.entity.Device;

public final class DeviceMapper {

    private DeviceMapper() {
    }

    public static DeviceDTO toDto(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .description(device.getDescription())
                .maximumConsumption(device.getMaximumConsumption())
                .powerConsumption(device.getPowerConsumption())
                .ownerId(device.getOwnerId())
                .build();
    }

    public static void update(Device device, DeviceRequest request) {
        device.setName(request.getName());
        device.setDescription(request.getDescription());
        device.setMaximumConsumption(request.getMaximumConsumption());
        device.setPowerConsumption(request.getPowerConsumption());
    }
}
