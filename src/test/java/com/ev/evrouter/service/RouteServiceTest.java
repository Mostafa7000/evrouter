package com.ev.evrouter.service;

import com.ev.evrouter.domain.Trip;
import com.ev.evrouter.dto.PlanRequest;
import com.ev.evrouter.dto.PlanResponse;
import com.ev.evrouter.exception.RoutePlanningException;
import com.ev.evrouter.io.OpenChargeMapClient;
import com.ev.evrouter.io.OpenRouteServiceClient;
import com.ev.evrouter.repository.TripRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

public class RouteServiceTest {

    @Mock
    private OpenRouteServiceClient routeServiceClient;

    @Mock
    private OpenChargeMapClient chargeMapClient;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private RouteService routeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ObjectNode createMockRouteData() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode routeData = mapper.createObjectNode();
        ObjectNode summary = mapper.createObjectNode()
                .put("distance", 250000)
                .put("duration", 10800)
                .put("ascent", 500)
                .put("descent", 300);
        ObjectNode properties = mapper.createObjectNode();
        properties.set("summary", summary);
        ObjectNode geometry = mapper.createObjectNode();
        geometry.putArray("coordinates").addArray().add(0).add(0);
        ObjectNode feature = mapper.createObjectNode();
        feature.set("properties", properties);
        feature.set("geometry", geometry);
        routeData.putArray("features").add(feature);
        routeData.putArray("bbox").add(0).add(0).add(0).add(0);
        return routeData;
    }

    @Test
    public void testPlan_CanMakeIt() {
        PlanRequest request = new PlanRequest();
        request.setStartLat(52.5200);
        request.setStartLon(13.4050);
        request.setEndLat(53.5511);
        request.setEndLon(9.9937);
        request.setBatteryCapacity(100.0);
        request.setConsumptionPerKm(0.2);
        request.setVehicleMassKg(2000);

        ObjectNode routeData = createMockRouteData();

        when(routeServiceClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(routeData);
        when(chargeMapClient.getChargingStations(any())).thenReturn(Collections.emptyList());
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArguments()[0]);

        PlanResponse response = routeService.plan(request);

        assertEquals("You can make it to your destination without charging.", response.getVerdict());
    }

    @Test
    public void testPlan_CannotMakeIt() {
        PlanRequest request = new PlanRequest();
        request.setStartLat(52.5200);
        request.setStartLon(13.4050);
        request.setEndLat(53.5511);
        request.setEndLon(9.9937);
        request.setBatteryCapacity(50.0);
        request.setConsumptionPerKm(0.2);
        request.setVehicleMassKg(2000);

        ObjectNode routeData = createMockRouteData();

        when(routeServiceClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(routeData);
        when(chargeMapClient.getChargingStations(any())).thenReturn(Collections.emptyList());
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArguments()[0]);

        PlanResponse response = routeService.plan(request);

        assertEquals("You will need to charge your vehicle to reach your destination.", response.getVerdict());
    }

    @Test
    public void testPlan_ThrowsException() {
        PlanRequest request = new PlanRequest();
        request.setStartLat(52.5200);
        request.setStartLon(13.4050);
        request.setEndLat(53.5511);
        request.setEndLon(9.9937);
        request.setBatteryCapacity(50.0);
        request.setConsumptionPerKm(0.2);
        request.setVehicleMassKg(2000);

        when(routeServiceClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenThrow(new RuntimeException("API error"));

        assertThrows(RoutePlanningException.class, () -> {
            routeService.plan(request);
        });
    }
}
