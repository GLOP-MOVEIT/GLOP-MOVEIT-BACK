package com.moveit.localization.service;

import com.moveit.localization.dto.*;
import java.util.List;
public interface LocationService {
    LocationResponse updateLocation(Integer userId, LocationUpdateRequest request);
    LocationResponse getLocation(Integer userId);
    List<LocationResponse> getNearbyUsers(Integer requesterId, LocationUpdateRequest request);
}
