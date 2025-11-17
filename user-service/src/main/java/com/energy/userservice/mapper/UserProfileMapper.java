package com.energy.userservice.mapper;

import com.energy.userservice.dto.UserProfileDTO;
import com.energy.userservice.entity.UserProfile;

public final class UserProfileMapper {

    private UserProfileMapper() {
    }

    public static UserProfileDTO toDto(UserProfile profile) {
        return UserProfileDTO.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .build();
    }

    public static void update(UserProfile profile, com.energy.userservice.dto.request.UserProfileRequest request) {
        if (profile.getId() == null) {
            profile.setId(request.getId());
        }
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
    }
}
