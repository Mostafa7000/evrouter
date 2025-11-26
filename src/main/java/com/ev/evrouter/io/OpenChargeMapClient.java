package com.ev.evrouter.io;

import com.ev.evrouter.dto.ChargingStation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OpenChargeMapClient {

    private final RestTemplate restTemplate;
    private static final double BUFFER_KM = 5.0;

    @Value("${openchargemap.api.key}")
    private String apiKey;

    public OpenChargeMapClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ChargingStation> getChargingStations(JsonNode boundingBox) {
        double minLat = boundingBox.get(1).asDouble();
        double minLon = boundingBox.get(0).asDouble();
        double maxLat = boundingBox.get(3).asDouble();
        double maxLon = boundingBox.get(2).asDouble();

        double latBuffer = BUFFER_KM / 111.32;
        double lonBuffer = BUFFER_KM / (111.32 * Math.cos(Math.toRadians((minLat + maxLat) / 2)));

        minLat -= latBuffer;
        minLon -= lonBuffer;
        maxLat += latBuffer;
        maxLon += lonBuffer;

        String url = String.format("https://api.openchargemap.io/v3/poi/?output=json&boundingbox=(%f,%f),(%f,%f)&distanceunit=KM&maxresults=1000&key=%s",
                minLat, minLon, maxLat, maxLon, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            List<ChargingStation> stations = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    ChargingStation station = new ChargingStation();
                    station.setLatitude(node.path("AddressInfo").path("Latitude").asDouble());
                    station.setLongitude(node.path("AddressInfo").path("Longitude").asDouble());
                    station.setName(node.path("AddressInfo").path("Title").asText());

                    Optional.ofNullable(node.path("OperatorInfo").path("Title").asText(null)).ifPresent(station::setOperator);

                    if (node.has("Connections") && node.get("Connections").isArray() && node.get("Connections").size() > 0) {
                        JsonNode connection = node.get("Connections").get(0);
                        Optional.ofNullable(connection.path("Level").path("Title").asText(null)).ifPresent(station::setSpeed);
                        Optional.ofNullable(connection.path("ConnectionType").path("Title").asText(null)).ifPresent(station::setConnectorType);
                    }
                    stations.add(station);
                }
            }
            return stations;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse charging station response", e);
        }
    }
}
