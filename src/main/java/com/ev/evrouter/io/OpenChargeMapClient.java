package com.ev.evrouter.io;

import com.ev.evrouter.dto.ChargingStation;
import com.ev.evrouter.dto.Connector;
import com.ev.evrouter.util.PolylineEncoder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OpenChargeMapClient {

    private final RestTemplate restTemplate;

    @Value("${openchargemap.api.key}")
    private String apiKey;

    public OpenChargeMapClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ChargingStation> getChargingStations(List<double[]> routePoints) {
        String encodedPolyline = PolylineEncoder.encode(routePoints);

        // FIX: Build a URI object, NOT a String.
        // Strings are treated as templates (expanding {}), URIs are treated as final.
        URI uri = UriComponentsBuilder.fromUri(URI.create("https://api.openchargemap.io/v3/poi/"))
                .queryParam("output", "json")
                .queryParam("polyline", encodedPolyline)
                .queryParam("distance", "5")
                .queryParam("distanceunit", "KM")
                .queryParam("maxresults", "100")
                .queryParam("key", apiKey)
                .build()
                .toUri(); // <--- Important: Returns java.net.URI
        String response;
        try {
            // Pass the URI object directly
            response = restTemplate.getForObject(uri, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to parse charging station response", e);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            List<ChargingStation> stations = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    ChargingStation station = new ChargingStation();
                    station.setLatitude(node.path("AddressInfo").path("Latitude").asDouble());
                    station.setLongitude(node.path("AddressInfo").path("Longitude").asDouble());
                    station.setName(node.path("AddressInfo").path("Title").asText());

                    // Set operator information if available
                    JsonNode operatorInfo = node.path("OperatorInfo");
                    if (!operatorInfo.isMissingNode()) {
                        Optional.ofNullable(operatorInfo.path("Title").asText(null)).ifPresent(station::setOperator);
                        Optional.ofNullable(operatorInfo.path("WebsiteURL").asText(null))
                                .ifPresent(station::setOperatorWebsite);
                    }

                    // Set usage type title (accessibility information)
                    JsonNode usageType = node.path("UsageType");
                    if (!usageType.isMissingNode()) {
                        Optional.ofNullable(usageType.path("Title").asText(null))
                                .ifPresent(station::setUsageType);
                    }

                    // Set station status (isLive)
                    boolean isLive = node.path("StatusType").path("IsOperational").asBoolean();
                    station.setLive(isLive);

                    // Set cost information if available
                    if (node.has("UsageCost")) {
                        station.setCost(node.path("UsageCost").asText("Pricing information not available"));
                    } else if (node.has("UsageType") && node.path("UsageType").has("IsPayAtLocation")) {
                        station.setCost("Pay at location");
                    } else {
                        station.setCost("Free");
                    }

                    // Set total number of bays (NumberOfPoints in OCM API)
                    int totalBays = node.path("NumberOfPoints").asInt(1); // Default to 1 if not specified
                    station.setTotalBays(totalBays);

                    if (node.has("Connections") && node.get("Connections").isArray()) {
                        for (JsonNode connection : node.get("Connections")) {
                            Connector connector = new Connector();
                            connector.setTitle(connection.path("ConnectionType").path("Title").asText(""));
                            connector.setSpeed(connection.path("Level").path("Title").asText(""));

                            // Set power in kilowatts (default to 0 if not available)
                            double powerKW = connection.path("PowerKW").asDouble(0);
                            // If PowerKW is 0, try to get it from the Level's PowerKW field
                            if (powerKW <= 0) {
                                powerKW = connection.path("Level").path("PowerKW").asDouble(0);
                            }
                            connector.setPowerKW(powerKW);

                            // Set quantity (default to 1 if not specified)
                            int quantity = connection.path("Quantity").asInt(1);
                            connector.setQuantity(quantity);

                            // Check if it's a fast charger (using both the flag and power level as fallback)
                            boolean isFastCharging = connection.path("Level").path("IsFastChargeCapable").asBoolean(false) ||
                                    powerKW >= 43; // Typically, 43kW+ is considered fast charging
                            connector.setFastCharging(isFastCharging);

                            // Check if the connector is operational
                            boolean isOperational = connection.path("StatusType").path("IsOperational").asBoolean();
                            connector.setOperational(isOperational);

                            station.getConnectors().add(connector);
                        }
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
