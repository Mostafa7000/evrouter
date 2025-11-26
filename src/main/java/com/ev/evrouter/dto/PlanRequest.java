package com.ev.evrouter.dto;

import lombok.Data;

@Data
public class PlanRequest {
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private double batteryCapacity;
    private double consumptionPerKm;
    private double vehicleMassKg;
}
