package com.ev.evrouter.service;

import com.ev.evrouter.domain.Trip;
import com.ev.evrouter.dto.ChargingStation;
import com.ev.evrouter.dto.PlanRequest;
import com.ev.evrouter.dto.PlanResponse;
import com.ev.evrouter.exception.RoutePlanningException;
import com.ev.evrouter.io.OpenChargeMapClient;
import com.ev.evrouter.io.OpenRouteServiceClient;
import com.ev.evrouter.repository.TripRepository;
import com.ev.evrouter.util.EVCalculator;
import com.ev.evrouter.util.GeometryUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            double ascent = routeData.path("features").get(0).path("properties").path("ascent").asDouble();
            double descent = routeData.path("features").get(0).path("properties").path("descent").asDouble();

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
            // Extract coordinates from GeoJSON and convert to List<double[]>
            List<double[]> coordinates = new ArrayList<>();
            JsonNode coordinatesNode = routeData.path("features").get(0).path("geometry").path("coordinates");
            if (coordinatesNode.isArray()) {
                for (JsonNode coordNode : coordinatesNode) {
                    if (coordNode.isArray() && coordNode.size() >= 2) {
                        double lon = coordNode.get(0).asDouble();
                        double lat = coordNode.get(1).asDouble();
                        coordinates.add(new double[]{lon, lat});
                    }
                }
            }
//            // 1. Get raw stations from the bounding box (keep this API call, it's efficient for fetching candidates)
//            List<ChargingStation> allStations = chargeMapClient.getChargingStations(routeData.path("bbox"));
//
//            // 2. Filter them against the route path
//            List<ChargingStation> validStations = allStations.stream()
//                    .filter(station -> GeometryUtil.isNearRoute(
//                            station.getLatitude(),
//                            station.getLongitude(),
//                            coordinates, // This is your 'routePolyline' list
//                            5.0 // 20km buffer
//                    ))
//                    .toList();

            // 3. Set the filtered list to the response
            response.setChargingStations(chargeMapClient.getChargingStations(coordinates));
            response.setVerdict(verdict);
            response.setRoutePolyline(coordinates);
            response.setDistance(distance);
            response.setEstimatedTime(summary.path("duration").asDouble());
            response.setEnergyRequired(energyRequired);
            response.setTotalAscent(ascent);
            response.setTotalDescent(descent);
            double remainingBattery = Math.max(0, request.getBatteryCapacity() - energyRequired);
            response.setBatteryOnArrival(remainingBattery);

            return response;
        } catch (Exception e) {
            throw new RoutePlanningException("Failed to plan route", e);
        }
    }
}
