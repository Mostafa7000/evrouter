package com.ev.evrouter.controller;

import com.ev.evrouter.dto.PlanRequest;
import com.ev.evrouter.dto.PlanResponse;
import com.ev.evrouter.exception.RoutePlanningException;
import com.ev.evrouter.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class PlanController {

    private final RouteService routeService;

    public PlanController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(summary = "Plan an EV route", description = "Calculate the optimal route for an electric vehicle considering charging stops")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route calculated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PlanResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error calculating route")
    })
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlanResponse> plan(@RequestBody PlanRequest request) {
        try {
            PlanResponse response = routeService.plan(request);
            return ResponseEntity.ok(response);
        } catch (RoutePlanningException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
