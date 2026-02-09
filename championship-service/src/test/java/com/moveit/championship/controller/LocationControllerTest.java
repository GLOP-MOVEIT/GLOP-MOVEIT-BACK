package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.championship.entity.Location;
import com.moveit.championship.exception.LocationNotFoundException;
import com.moveit.championship.mother.LocationMother;
import com.moveit.championship.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationController.class)
@Import(ObjectMapper.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationService locationService;

    private Location location1;
    private Location location2;

    @BeforeEach
    void setUp() {
        location1 = LocationMother.location().build();
        location2 = LocationMother.location()
                .withLocationId(2)
                .withName("Parc des Princes")
                .withLatitude(48.8414)
                .withLongitude(2.2530)
                .build();
    }

    @Test
    @DisplayName("Should retrieve all locations successfully")
    void getAllLocations_shouldReturnList() throws Exception {
        when(locationService.getAllLocations()).thenReturn(List.of(location1, location2));

        mockMvc.perform(get("/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].locationId", equalTo(location1.getLocationId())))
                .andExpect(jsonPath("$[0].name", equalTo(location1.getName())))
                .andExpect(jsonPath("$[1].locationId", equalTo(location2.getLocationId())))
                .andExpect(jsonPath("$[1].name", equalTo(location2.getName())));

        verify(locationService, times(1)).getAllLocations();
    }

    @Test
    @DisplayName("Should retrieve location by ID successfully")
    void getLocationById_shouldReturnLocation() throws Exception {
        when(locationService.getLocationById(1)).thenReturn(location1);

        mockMvc.perform(get("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId", equalTo(location1.getLocationId())))
                .andExpect(jsonPath("$.name", equalTo(location1.getName())))
                .andExpect(jsonPath("$.latitude", equalTo(location1.getLatitude())))
                .andExpect(jsonPath("$.longitude", equalTo(location1.getLongitude())));

        verify(locationService, times(1)).getLocationById(1);
    }

    @Test
    @DisplayName("Should return 404 when location not found")
    void getLocationById_shouldReturn404_whenNotFound() throws Exception {
        when(locationService.getLocationById(999)).thenThrow(new LocationNotFoundException(999));

        mockMvc.perform(get("/locations/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).getLocationById(999);
    }

    @Test
    @DisplayName("Should create location successfully")
    void createLocation_shouldReturnCreated() throws Exception {
        when(locationService.createLocation(any(Location.class))).thenReturn(location1);

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(location1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId", equalTo(location1.getLocationId())))
                .andExpect(jsonPath("$.name", equalTo(location1.getName())));

        verify(locationService, times(1)).createLocation(any(Location.class));
    }

    @Test
    @DisplayName("Should update location successfully")
    void updateLocation_shouldReturnUpdated() throws Exception {
        Location updatedLocation = LocationMother.location()
                .withName("Stade Modifié")
                .build();

        when(locationService.updateLocation(eq(1), any(Location.class))).thenReturn(updatedLocation);

        mockMvc.perform(put("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLocation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Stade Modifié")));

        verify(locationService, times(1)).updateLocation(eq(1), any(Location.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent location")
    void updateLocation_shouldReturn404_whenNotFound() throws Exception {
        when(locationService.updateLocation(eq(999), any(Location.class)))
                .thenThrow(new LocationNotFoundException(999));

        mockMvc.perform(put("/locations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(location1)))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).updateLocation(eq(999), any(Location.class));
    }

    @Test
    @DisplayName("Should delete location successfully")
    void deleteLocation_shouldReturnNoContent() throws Exception {
        doNothing().when(locationService).deleteLocation(1);

        mockMvc.perform(delete("/locations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(locationService, times(1)).deleteLocation(1);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent location")
    void deleteLocation_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new LocationNotFoundException(999)).when(locationService).deleteLocation(999);

        mockMvc.perform(delete("/locations/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).deleteLocation(999);
    }
}
