package com.moveit.localization.service.impl;

import com.moveit.localization.dto.*;
import com.moveit.localization.entity.UserLocation;
import com.moveit.localization.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements com.moveit.localization.service.LocationService {

        private final UserLocationRepository userLocationRepository;
    private static final double DEFAULT_RADIUS_KM = 5.0;


    @Transactional
    public LocationResponse updateLocation(Integer userId, LocationUpdateRequest request) {


        // Mettre à jour ou créer la localisation actuelle
        UserLocation location = userLocationRepository.findByUserId(userId)
                .orElse(UserLocation.builder()
                        .userId(userId)
                        .build());

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setUpdatedAt(LocalDateTime.now());

        userLocationRepository.save(location);
        return mapToLocationResponse(location);
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocation(Integer userId) {
        UserLocation location = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Location not found for user: " + userId));
        return mapToLocationResponse(location);
    }



    @Transactional(readOnly = true)
    public List<LocationResponse> getNearbyUsers(Integer requesterId, LocationUpdateRequest request) {

        List<UserLocation> nearbyUsers = userLocationRepository.findNearbyUsers(
                request.getLatitude(),
                request.getLongitude(),
                DEFAULT_RADIUS_KM,
                requesterId
        );

        return nearbyUsers.stream()
                .map(this::mapToLocationResponse)
                .collect(Collectors.toList());
    }



    private LocationResponse mapToLocationResponse(UserLocation location) {
        return LocationResponse.builder()
                .userId(location.getUserId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .updatedAt(location.getUpdatedAt())
                .build();
    }


}
