package com.moveit.localization.controller;

import com.moveit.localization.dto.*;
import com.moveit.localization.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "Location tracking operations")
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/update")
    @Operation(summary = "Update user location", description = "Update current location for a user")
    public ResponseEntity<LocationResponse> updateLocation(
            @RequestHeader("X-User-Id") Integer userId,
            @Valid @RequestBody LocationUpdateRequest request) {
        LocationResponse response = locationService.updateLocation(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user location", description = "Get current location of a specific user")
    public ResponseEntity<LocationResponse> getLocation(@PathVariable Integer userId) {
        LocationResponse response = locationService.getLocation(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/nearby")
    @Operation(summary = "Find nearby users", description = "Find users near a specific location")
    public ResponseEntity<List<LocationResponse>> getNearbyUsers(
            @RequestHeader("X-User-Id") Integer requesterId,
            @Valid @RequestBody LocationUpdateRequest request) {
        List<LocationResponse> response = locationService.getNearbyUsers(requesterId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get location history", description = "Get location history for a athlete (commissaire only)")
    public ResponseEntity<List<LocationHistoryResponse>> getLocationHistory(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "7") int days) {
        List<LocationHistoryResponse> response = locationService.getLocationHistory(userId, days);
        return ResponseEntity.ok(response);
    }
}
