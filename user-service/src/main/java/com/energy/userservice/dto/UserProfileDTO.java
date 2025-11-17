package com.energy.userservice.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileDTO {
    UUID id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String address;
    String city;
    String country;
}
