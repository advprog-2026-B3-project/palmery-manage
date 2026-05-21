package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.config.AuthIntegrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Component
public class AuthUserClient {

    private final AuthIntegrationProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private String cachedServiceToken;
    private long serviceTokenExpiresAtEpochMs;

    public AuthUserClient(AuthIntegrationProperties properties) {
        this.properties = properties;
    }

    public Map<UUID, UserSummary> fetchUsersByIds(List<UUID> ids) {
        if (!properties.isEnabled() || ids == null || ids.isEmpty()) {
            return Map.of();
        }

        try {
            String token = obtainServiceToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<String> idStrings = ids.stream().map(UUID::toString).toList();
            HttpEntity<List<String>> request = new HttpEntity<>(idStrings, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/api/users/by-ids",
                    HttpMethod.POST,
                    request,
                    List.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Map.of();
            }

            Map<UUID, UserSummary> result = new HashMap<>();
            for (Object row : response.getBody()) {
                if (row instanceof Map<?, ?> map) {
                    UserSummary summary = fromMap(map);
                    if (summary != null) {
                        result.put(summary.id(), summary);
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("Failed to fetch users from auth service: {}", ex.getMessage());
            return Map.of();
        }
    }

    public List<UserSummary> fetchUsersByRole(String role) {
        if (!properties.isEnabled() || role == null || role.isBlank()) {
            return List.of();
        }

        try {
            String token = obtainServiceToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                            .path("/api/users")
                            .queryParam("role", role)
                            .toUriString(),
                    HttpMethod.GET,
                    request,
                    List.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return List.of();
            }

            return response.getBody().stream()
                    .filter(Map.class::isInstance)
                    .map(row -> fromMap((Map<?, ?>) row))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to fetch users by role from auth service: {}", ex.getMessage());
            return List.of();
        }
    }

    private UserSummary fromMap(Map<?, ?> map) {
        Object idObj = map.get("id");
        Object namaObj = map.get("nama");
        if (idObj == null) {
            return null;
        }
        try {
            UUID id = UUID.fromString(idObj.toString());
            String nama = namaObj == null ? idObj.toString() : namaObj.toString();
            String email = map.get("email") == null ? "" : map.get("email").toString();
            return new UserSummary(id, nama, email);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private synchronized String obtainServiceToken() {
        if (cachedServiceToken != null && System.currentTimeMillis() < serviceTokenExpiresAtEpochMs) {
            return cachedServiceToken;
        }

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "client_id", properties.getServiceClientId(),
                "client_secret", properties.getServiceClientSecret()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                properties.getBaseUrl() + "/api/auth/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new IllegalStateException("Auth service token response invalid");
        }

        cachedServiceToken = responseBody.get("access_token").toString();
        Object expiresIn = responseBody.get("expires_in");
        long ttlSeconds = expiresIn instanceof Number number ? number.longValue() : 3600L;
        serviceTokenExpiresAtEpochMs = System.currentTimeMillis() + (ttlSeconds - 60) * 1000;
        return cachedServiceToken;
    }

    public record UserSummary(UUID id, String nama, String email) {
    }
}
