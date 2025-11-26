package com.ev.evrouter.service;

import com.ev.evrouter.domain.Trip;
import com.ev.evrouter.dto.PlanRequest;
import com.ev.evrouter.dto.PlanResponse;
import com.ev.evrouter.exception.RoutePlanningException;
import com.ev.evrouter.io.OpenChargeMapClient;
import com.ev.evrouter.io.OpenRouteServiceClient;
import com.ev.evrouter.repository.TripRepository;
import com.ev.evrouter.util.EVCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final OpenRouteServiceClient routeServiceClient;
    private final OpenChargeMapClient chargeMapClient;
    private final TripRepository tripRepository;

    public RouteService(OpenRouteServiceClient routeServiceClient, OpenChargeMapClient chargeMapClient, TripRepository tripRepository) {
        this.routeServiceClient = routeServiceClient;
        this.chargeMapClient = chargeMapClient;
        this.tripRepository = tripRepository;
    }

    public PlanResponse plan(PlanRequest request) {
        try {
            JsonNode routeData = routeServiceClient.getRoute(request.getStartLat(), request.getStartLon(), request.getEndLat(), request.getEndLon());

            JsonNode summary = routeData.path("features").get(0).path("properties").path("summary");
            double distance = summary.path("distance").asDouble() / 1000;
            double ascent = summary.path("ascent").asDouble();
            double descent = summary.path("descent").asDouble();

            double energyRequired = EVCalculator.calculateConsumption(distance, ascent, descent, request.getConsumptionPerKm(), request.getVehicleMassKg());
            boolean canMakeIt = request.getBatteryCapacity() >= energyRequired;
            String verdict = canMakeIt ? "You can make it to your destination without charging." : "You will need to charge your vehicle to reach your destination.";

            Trip trip = new Trip();
            trip.setStartLat(request.getStartLat());
            trip.setStartLon(request.getStartLon());
            trip.setEndLat(request.getEndLat());
            trip.setEndLon(request.getEndLon());
            trip.setBatteryCapacity(request.getBatteryCapacity());
            trip.setConsumptionPerKm(request.getConsumptionPerKm());
            trip.setVerdict(verdict);
            tripRepository.save(trip);

            PlanResponse response = new PlanResponse();
            response.setVerdict(verdict);
            response.setRoutePolyline(routeData.path("features").get(0).path("geometry").path("coordinates"));
            response.setChargingStations(chargeMapClient.getChargingStations(routeData.path("bbox")));
            response.setDistance(distance);
            response.setEstimatedTime(summary.path("duration").asDouble());
            response.setEnergyRequired(energyRequired);

            return response;
        } catch (Exception e) {
            throw new RoutePlanningException("Failed to plan route", e);
        }
    }
}
