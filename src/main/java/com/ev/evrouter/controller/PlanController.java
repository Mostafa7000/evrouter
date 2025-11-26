package com.ev.evrouter.controller;

import com.ev.evrouter.dto.PlanRequest;
import com.ev.evrouter.dto.PlanResponse;
import com.ev.evrouter.exception.RoutePlanningException;
import com.ev.evrouter.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PlanController {

    private final RouteService routeService;

    public PlanController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/plan")
    public ResponseEntity<?> plan(@RequestBody PlanRequest request) {
        try {
            PlanResponse response = routeService.plan(request);
            return ResponseEntity.ok(response);
        } catch (RoutePlanningException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
