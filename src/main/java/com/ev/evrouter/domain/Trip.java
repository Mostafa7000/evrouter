package com.ev.evrouter.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private double batteryCapacity;
    private double consumptionPerKm;
    private String verdict;
}
