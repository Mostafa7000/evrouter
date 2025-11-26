package com.ev.evrouter.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Component
public class OpenRouteServiceClient {

    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    public OpenRouteServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode getRoute(double startLat, double startLon, double endLat, double endLon) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", apiKey);

        String requestBody = String.format("{\"coordinates\":[[%s,%s],[%s,%s]],\"elevation\":\"true\"}",
                startLon, startLat, endLon, endLat);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            if (root.has("features") && root.get("features").isArray() && root.get("features").size() > 0) {
                return root;
            } else {
                if (root.has("error")) {
                    throw new RuntimeException("Error from OpenRouteService: " + root.get("error").toString());
                }
                throw new RuntimeException("Invalid route response: 'features' array is missing or empty.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse route response", e);
        }
    }
}
