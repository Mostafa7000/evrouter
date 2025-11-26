package com.ev.evrouter.dto;

import lombok.Data;

@Data
public class ChargingStation {
    private double latitude;
    private double longitude;
    private String name;
    private String operator;
    private String speed;
    private String connectorType;
}
