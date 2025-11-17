package com.energy.userservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;

@Value
public class DeviceDTO {
    UUID id;
    String name;
    BigDecimal maximumConsumption;
    UUID ownerId;
}
