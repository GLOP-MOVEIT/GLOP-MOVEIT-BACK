package com.moveit.location.service;

import com.moveit.location.entity.Location;
import com.moveit.location.exception.LocationNotFoundException;
import com.moveit.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Integer id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Transactional
    public Location createLocation(Location location) {
        location.setLocationId(null);
        return locationRepository.save(location);
    }

    @Transactional
    public Location updateLocation(Integer id, Location location) {
        Location existingLocation = getLocationById(id);
        
        existingLocation.setName(location.getName());
        existingLocation.setLatitude(location.getLatitude());
        existingLocation.setLongitude(location.getLongitude());
        existingLocation.setMainEntrance(location.getMainEntrance());
        existingLocation.setRefereeEntrance(location.getRefereeEntrance());
        existingLocation.setAthleteEntrance(location.getAthleteEntrance());
        existingLocation.setVipEntrance(location.getVipEntrance());
        existingLocation.setDescription(location.getDescription());
        
        return locationRepository.save(existingLocation);
    }

    @Transactional
    public void deleteLocation(Integer id) {
        Location location = getLocationById(id);
        locationRepository.delete(location);
    }
}
