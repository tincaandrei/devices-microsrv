package com.energy.deviceservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DeviceRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "0.1")
    private BigDecimal maximumConsumption;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal powerConsumption;
}
