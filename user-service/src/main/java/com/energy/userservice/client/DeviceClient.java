package com.energy.userservice.client;

import com.energy.userservice.dto.DeviceDTO;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DeviceClient {

    private final RestTemplate restTemplate;

    @Value("${services.device.base-url}")
    private String deviceServiceBaseUrl;

    public List<DeviceDTO> getDevicesByOwner(UUID ownerId, String authorizationHeader) {
        String url = deviceServiceBaseUrl + "/devices/owner/" + ownerId;
        HttpHeaders headers = new HttpHeaders();
        // Internal cross-service call: act as admin when querying Device Service.
        // External access is still controlled at the API gateway + User Service layer.
        headers.set("X-Role", "ROLE_ADMIN");
        headers.set("X-User-Id", ownerId.toString());
        if (authorizationHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        ResponseEntity<List<DeviceDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody() == null ? Collections.emptyList() : response.getBody();
    }
}
