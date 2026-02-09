package com.moveit.location.service;

import com.moveit.location.entity.Location;
import com.moveit.location.exception.LocationNotFoundException;
import com.moveit.location.mother.LocationMother;
import com.moveit.location.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private Location location;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        location = LocationMother.location().build();
    }

    @Test
    @DisplayName("Should return all locations")
    void getAllLocations_shouldReturnAllLocations() {
        Location location2 = LocationMother.location()
                .withLocationId(2)
                .withName("Parc des Princes")
                .build();

        when(locationRepository.findAll()).thenReturn(List.of(location, location2));

        List<Location> result = locationService.getAllLocations();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(location, location2);
        verify(locationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return location by ID when exists")
    void getLocationById_shouldReturnLocation_whenExists() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));

        Location result = locationService.getLocationById(1);

        assertThat(result).isEqualTo(location);
        verify(locationRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw LocationNotFoundException when location not found")
    void getLocationById_shouldThrow_whenNotFound() {
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getLocationById(999))
                .isInstanceOf(LocationNotFoundException.class);
        
        verify(locationRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should create location successfully")
    void createLocation_shouldSaveAndReturnLocation() {
        location.setLocationId(null);
        Location savedLocation = LocationMother.location().build();

        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        Location result = locationService.createLocation(location);

        assertThat(result).isEqualTo(savedLocation);
        assertThat(result.getLocationId()).isNotNull();
        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    @DisplayName("Should update location successfully")
    void updateLocation_shouldUpdateAndReturnLocation() {
        Location updatedData = LocationMother.location()
                .withName("Nouveau Stade")
                .withLatitude(48.8566)
                .withLongitude(2.3522)
                .build();

        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));

        Location result = locationService.updateLocation(1, updatedData);

        assertThat(result.getName()).isEqualTo("Nouveau Stade");
        assertThat(result.getLatitude()).isEqualTo(48.8566);
        assertThat(result.getLongitude()).isEqualTo(2.3522);
        verify(locationRepository, times(1)).findById(1);
        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    @DisplayName("Should throw LocationNotFoundException when updating non-existent location")
    void updateLocation_shouldThrow_whenNotFound() {
        Location updatedData = LocationMother.location().build();
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(999, updatedData))
                .isInstanceOf(LocationNotFoundException.class);
        
        verify(locationRepository, times(1)).findById(999);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    @DisplayName("Should delete location successfully")
    void deleteLocation_shouldDelete_whenExists() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        doNothing().when(locationRepository).delete(location);

        locationService.deleteLocation(1);

        verify(locationRepository, times(1)).findById(1);
        verify(locationRepository, times(1)).delete(location);
    }

    @Test
    @DisplayName("Should throw LocationNotFoundException when deleting non-existent location")
    void deleteLocation_shouldThrow_whenNotFound() {
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.deleteLocation(999))
                .isInstanceOf(LocationNotFoundException.class);
        
        verify(locationRepository, times(1)).findById(999);
        verify(locationRepository, never()).delete(any(Location.class));
    }
}
