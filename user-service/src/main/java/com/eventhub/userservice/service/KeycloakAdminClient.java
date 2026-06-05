package com.eventhub.userservice.service;

import com.eventhub.userservice.KeycloakAdminProperties;
import com.eventhub.userservice.service.exception.UserProvisioningException;
import com.eventhub.userservice.web.dto.CreateUserRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {
    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    public String createUser(CreateUserRequest request) {
        var token = accessToken();
        var response = restClient.post()
                .uri(adminUsersUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "username", request.username(),
                        "email", request.email() == null ? "" : request.email(),
                        "enabled", true,
                        "emailVerified", true,
                        "credentials", List.of(Map.of(
                                "type", "password",
                                "value", request.password(),
                                "temporary", false
                        ))
                ))
                .retrieve()
                .toBodilessEntity();
        var location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null || location.isBlank()) {
            throw new UserProvisioningException("Keycloak did not return created user location");
        }
        var userId = location.substring(location.lastIndexOf('/') + 1);
        assignRealmRole(token, userId, "USER");
        return userId;
    }

    public void disableUser(String userId) {
        restClient.put()
                .uri(adminUsersUri() + "/" + userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", false))
                .retrieve()
                .toBodilessEntity();
    }

    private void assignRealmRole(String token, String userId, String roleName) {
        var role = restClient.get()
                .uri(adminRealmRoleUri(roleName))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Map.class);
        if (role == null) {
            throw new UserProvisioningException("Keycloak role not found: " + roleName);
        }
        restClient.post()
                .uri(adminUsersUri() + "/" + userId + "/role-mappings/realm")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }

    private String accessToken() {
        var response = restClient.post()
                .uri(properties.baseUrl() + "/realms/" + properties.realm() + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&client_id=" + properties.clientId() + "&client_secret=" + properties.clientSecret())
                .retrieve()
                .body(Map.class);
        if (response == null || response.get("access_token") == null) {
            throw new UserProvisioningException("Failed to obtain Keycloak admin token");
        }
        return response.get("access_token").toString();
    }

    private String adminUsersUri() {
        return properties.baseUrl() + "/admin/realms/" + properties.realm() + "/users";
    }

    private String adminRealmRoleUri(String roleName) {
        return properties.baseUrl() + "/admin/realms/" + properties.realm() + "/roles/" + roleName;
    }
}
