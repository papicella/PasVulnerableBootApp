package com.example.pasvulnerablebootapp.rest;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class GetTime {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TIMEZONES_URL = "https://worldtimeapi.org/api/timezone";
    private static final String TIMEZONE_URL_TEMPLATE = "https://worldtimeapi.org/api/timezone/{zone}";

    @GetMapping("/time")
    public ResponseEntity<?> getTime(@RequestParam("city") String city) {
        if (city == null || city.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "city parameter is required"));
        }

        try {
            String normalized = city.trim().replace(' ', '_');

            // Fetch list of all timezones, then try to match on the last segment being the city
            List<String> zones = restTemplate.exchange(
                    TIMEZONES_URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<String>>() {}
            ).getBody();

            if (zones == null || zones.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Unable to fetch timezones"));
            }

            Optional<String> matchedZone = zones.stream()
                    .filter(z -> z.toLowerCase(Locale.ROOT)
                            .endsWith("/" + normalized.toLowerCase(Locale.ROOT)))
                    .findFirst();

            if (matchedZone.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "City not found", "city", city));
            }

            Map<?, ?> timeData = restTemplate.getForObject(TIMEZONE_URL_TEMPLATE, Map.class, matchedZone.get());

            if (timeData == null || !timeData.containsKey("datetime")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Unable to retrieve time for zone", "zone", matchedZone.get()));
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("city", city);
            result.put("zone", matchedZone.get());
            result.put("datetime", timeData.get("datetime"));

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error", "message", ex.getMessage()));
        }
    }
}
