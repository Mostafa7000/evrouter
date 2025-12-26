package com.ev.evrouter.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChargingStation {
    private double latitude;
    private double longitude;
    private String name;
    private String operator;
    private String operatorWebsite; // URL of the operator's website
    private String usageType; // Indicates station accessibility (e.g., Public, Private, etc.)
    private String cost; // Cost information (e.g., "5 EGP/kWh + 20 EGP parking fee")
    private boolean isLive; // Global status for the station
    private int totalBays; // Number of physical parking bays available
    private List<Connector> connectors = new ArrayList<>();
}
