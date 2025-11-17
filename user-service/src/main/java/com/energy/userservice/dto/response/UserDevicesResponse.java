package com.energy.userservice.dto.response;

import com.energy.userservice.dto.DeviceDTO;
import com.energy.userservice.dto.UserProfileDTO;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDevicesResponse {
    UserProfileDTO user;
    List<DeviceDTO> devices;
}
