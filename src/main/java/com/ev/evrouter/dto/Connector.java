package com.ev.evrouter.dto;

import lombok.Data;

@Data
public class Connector {
    private String title;
    private String speed;
    private double powerKW; // Power in kilowatts, vital for calculating charge time
    private int quantity; // Number of this type of connector at the station
    private boolean fastCharging;
    private boolean operational;
}
