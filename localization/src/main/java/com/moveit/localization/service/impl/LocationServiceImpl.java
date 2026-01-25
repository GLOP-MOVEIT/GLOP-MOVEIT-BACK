package com.moveit.localization.service.impl;

import com.moveit.localization.dto.*;
import com.moveit.localization.entity.UserLocation;
import com.moveit.localization.repository.UserLocationRepository;
import com.moveit.localization.entity.LocationHistory;
import com.moveit.localization.repository.LocationHistoryRepository;
import com.moveit.localization.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.moveit.localization.entity.LocationHistory;
import com.moveit.localization.repository.LocationHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

        private final UserLocationRepository userLocationRepository;
        private final LocationHistoryRepository locationHistoryRepository;
        private static final long MIN_TIME_SECONDS = 300;
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

                // Historique uniquement pour les athlètes
                saveHistoryIfNeeded(userId, request);

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

    private void saveHistoryIfNeeded(Integer userId, LocationUpdateRequest request) {
        if (!isAthlete(userId)) {
            return;
        }
        LocationHistory last = locationHistoryRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream().findFirst().orElse(null);
        boolean shouldSave = false;
        LocalDateTime now = LocalDateTime.now();
        if (last == null) {
            shouldSave = true;
        } else {
            long seconds = java.time.Duration.between(last.getTimestamp(), now).getSeconds();
            if (seconds > MIN_TIME_SECONDS) {
                shouldSave = true;
            }
        }
        if (shouldSave) {
            LocationHistory history = LocationHistory.builder()
                    .userId(userId)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .timestamp(now)
                    .build();
            locationHistoryRepository.save(history);
        }
    }

    private boolean isAthlete(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ATHLETE") || role.equals("ATHLETE"));
    }
}
