package com.moveit.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.location.dto.BulkLocateTrialRequest;
import com.moveit.location.dto.BulkLocateTrialResponse;
import com.moveit.location.dto.BulkLocateUserPosition;
import com.moveit.location.mapper.LocationMapper;
import com.moveit.location.service.LocationLocatorService;
import com.moveit.location.service.LocationService;
import com.moveit.location.service.exception.UserServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationController.class)
@DisplayName("POST /locations/locate/trial/* - Localisation en masse (épreuve)")
class LocationControllerBulkLocateTrialTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private LocationLocatorService locatorService;

    @MockitoBean
    private LocationMapper locationMapper;

    @Test
    @DisplayName("/locate/trial/athletes renvoie uniquement la liste athletes")
    void locateAthletesForTrial_shouldReturnOnlyAthletes() throws Exception {
        BulkLocateTrialResponse serviceResponse = new BulkLocateTrialResponse(
                10,
                List.of(new BulkLocateUserPosition(1, "Alice", "Athlete", 48.86, 2.36)),
                List.of(new BulkLocateUserPosition(2, "Victor", "Volunteer", 48.87, 2.35))
        );
        when(locatorService.locateAllForTrial(any(BulkLocateTrialRequest.class), any())).thenReturn(serviceResponse);

        BulkLocateTrialRequest req = new BulkLocateTrialRequest(99, 10);

        mockMvc.perform(post("/locations/locate/trial/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(10))
                .andExpect(jsonPath("$.athletes.length()").value(1))
                .andExpect(jsonPath("$.athletes[0].userId").value(1))
                .andExpect(jsonPath("$.athletes[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.athletes[0].surname").value("Athlete"))
                .andExpect(jsonPath("$.volunteers.length()").value(0));
    }

    @Test
    @DisplayName("/locate/trial/volunteers renvoie uniquement la liste volunteers")
    void locateVolunteersForTrial_shouldReturnOnlyVolunteers() throws Exception {
        BulkLocateTrialResponse serviceResponse = new BulkLocateTrialResponse(
                10,
                List.of(new BulkLocateUserPosition(1, "Alice", "Athlete", 48.86, 2.36)),
                List.of(new BulkLocateUserPosition(2, "Victor", "Volunteer", 48.87, 2.35))
        );
        when(locatorService.locateAllForTrial(any(BulkLocateTrialRequest.class), any())).thenReturn(serviceResponse);

        BulkLocateTrialRequest req = new BulkLocateTrialRequest(99, 10);

        mockMvc.perform(post("/locations/locate/trial/volunteers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(10))
                .andExpect(jsonPath("$.volunteers.length()").value(1))
                .andExpect(jsonPath("$.volunteers[0].userId").value(2))
                .andExpect(jsonPath("$.volunteers[0].firstName").value("Victor"))
                .andExpect(jsonPath("$.volunteers[0].surname").value("Volunteer"))
                .andExpect(jsonPath("$.athletes.length()").value(0));
    }

    @Test
    @DisplayName("/locate/trial/athletes renvoie 403 si non autorisé")
    void locateAthletesForTrial_shouldReturn403_whenForbidden() throws Exception {
        when(locatorService.locateAllForTrial(any(BulkLocateTrialRequest.class), any()))
                .thenThrow(new UserServiceException("Non autorisé"));

        BulkLocateTrialRequest req = new BulkLocateTrialRequest(99, 10);

        mockMvc.perform(post("/locations/locate/trial/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/locate/trial/volunteers renvoie 403 si non autorisé")
    void locateVolunteersForTrial_shouldReturn403_whenForbidden() throws Exception {
        when(locatorService.locateAllForTrial(any(BulkLocateTrialRequest.class), any()))
                .thenThrow(new UserServiceException("Non autorisé"));

        BulkLocateTrialRequest req = new BulkLocateTrialRequest(99, 10);

        mockMvc.perform(post("/locations/locate/trial/volunteers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}

