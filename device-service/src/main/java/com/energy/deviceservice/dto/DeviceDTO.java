package com.energy.deviceservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeviceDTO {
    UUID id;
    String name;
    String description;
    BigDecimal maximumConsumption;
    BigDecimal powerConsumption;
    UUID ownerId;
}
