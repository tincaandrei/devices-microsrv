package com.energy.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

@Data
public class UserProfileRequest {

    @NotNull(message = "User id is required")
    private UUID id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6, max = 32)
    private String phoneNumber;

    private String address;
    private String city;
    private String country;
}
