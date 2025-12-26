package com.ev.evrouter.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Response containing the planned EV route and charging stops")
public class PlanResponse {
    @Schema(description = "Summary of the route planning result", example = "ROUTE_FOUND")
    private String verdict;
    @ArraySchema(
        arraySchema = @Schema(description = "List of coordinates representing the route polyline"),
        minItems = 1,
        schema = @Schema(
            type = "array",
            description = "[longitude, latitude] coordinates"
        )
    )
    private List<double[]> routePolyline;
    @ArraySchema(schema = @Schema(implementation = ChargingStation.class))
    private List<ChargingStation> chargingStations;
    @Schema(description = "Total distance of the route in kilometers", example = "125.5")
    private double distance;
    @Schema(description = "Estimated travel time in minutes", example = "95.0")
    private double estimatedTime;
    @Schema(description = "Total energy required for the trip in kWh", example = "35.2")
    private double energyRequired;
}
