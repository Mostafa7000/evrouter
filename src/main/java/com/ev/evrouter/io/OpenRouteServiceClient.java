package com.ev.evrouter.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class OpenRouteServiceClient {

    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    public OpenRouteServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode getRoute(double startLat, double startLon, double endLat, double endLon) {
        String url = String.format("https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%s,%s&end=%s,%s",
                apiKey, startLon, startLat, endLon, endLat);
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            if (root.has("features") && root.get("features").isArray() && root.get("features").size() > 0) {
                return root;
            } else {
                throw new RuntimeException("Invalid route response: 'features' array is missing or empty.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse route response", e);
        }
    }
}
