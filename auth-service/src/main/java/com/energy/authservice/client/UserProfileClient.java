package com.energy.authservice.client;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserProfileClient {

    private static final Logger log = LoggerFactory.getLogger(UserProfileClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url}")
    private String userServiceBaseUrl;

    public void createUserProfile(UUID id, String username, String email) {
        if (id == null) {
            return;
        }
        if (email == null || email.isBlank()) {
            // UserService requires a non-blank email for profiles; skip auto-create if missing.
            return;
        }

        String url = userServiceBaseUrl + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Internal service-to-service call; mark as admin so UserService accepts creation.
        headers.set("X-Role", "ROLE_ADMIN");

        UserProfileCreateRequest body = new UserProfileCreateRequest(
                id,
                username != null && !username.isBlank() ? username : "User",
                username != null && !username.isBlank() ? username : "User",
                email,
                null,
                null,
                null,
                null
        );

        try {
            HttpEntity<UserProfileCreateRequest> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Failed to auto-create user profile for credential {}. Status: {}",
                        id, response.getStatusCode());
            }
        } catch (Exception ex) {
            log.warn("Exception while auto-creating user profile for credential {}", id, ex);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UserProfileCreateRequest {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String country;
    }
}

