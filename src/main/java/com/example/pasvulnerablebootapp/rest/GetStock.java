package com.example.pasvulnerablebootapp.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GetStock {

    // Twelve Data API endpoint
    private static final String TWELVE_DATA_URL = "https://api.twelvedata.com/quote";
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${twelvedata.apikey:YOUR_API_KEY_HERE}")
    private String apiKey;

    @GetMapping("/stock")
    public ResponseEntity<?> restPage(@RequestParam("symbol") String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "symbol parameter is required"));
        }
        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Twelve Data API key is not set. Please register for a free API key at https://twelvedata.com/ and set it in application.properties as twelvedata.apikey."));
        }
        String symbolUpper = symbol.trim().toUpperCase();
        String url = UriComponentsBuilder.fromHttpUrl(TWELVE_DATA_URL)
                .queryParam("symbol", symbolUpper)
                .queryParam("apikey", apiKey)
                .toUriString();
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.containsKey("code")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No stock data found for symbol", "symbol", symbolUpper));
            }
            Map<String, Object> result = new HashMap<>();
            Object symbolVal = response.get("symbol");
            Object nameVal = response.get("name");
            Object priceVal = response.get("price");
            Object currencyVal = response.get("currency");
            Object exchangeVal = response.get("exchange");
            Object marketStateVal = response.get("exchange_timezone");
            Object percentChangeVal = response.get("percent_change");
            result.put("symbol", symbolVal != null ? symbolVal : symbolUpper);
            result.put("shortName", nameVal != null ? nameVal : "N/A");
            result.put("regularMarketPrice", priceVal != null ? priceVal : "N/A");
            result.put("currency", currencyVal != null ? currencyVal : "N/A");
            result.put("exchangeName", exchangeVal != null ? exchangeVal : "N/A");
            result.put("marketState", marketStateVal != null ? marketStateVal : "N/A");
            result.put("regularMarketChangePercent", percentChangeVal != null ? percentChangeVal : "N/A");
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + ex.getClass().getSimpleName(), "message", ex.getMessage()));
        }
    }
}
