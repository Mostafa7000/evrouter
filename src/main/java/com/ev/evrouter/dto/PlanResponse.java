package com.ev.evrouter.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class PlanResponse {
    private String verdict;
    private JsonNode routePolyline;
    private List<ChargingStation> chargingStations;
    private double distance;
    private double estimatedTime;
    private double energyRequired;
}
