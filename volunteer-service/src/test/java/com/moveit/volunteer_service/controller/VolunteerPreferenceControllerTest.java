package com.moveit.volunteer_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.volunteer_service.config.TestJacksonConfig;
import com.moveit.volunteer_service.dto.CheckVolunteerPreferencesRequest;
import com.moveit.volunteer_service.dto.CreateVolunteerPreferenceRequest;
import com.moveit.volunteer_service.dto.VolunteerWithPreferenceDTO;
import com.moveit.volunteer_service.exception.VolunteerPreferenceNotFoundException;
import com.moveit.volunteer_service.mother.VolunteerPreferenceMother;
import com.moveit.volunteer_service.service.VolunteerPreferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = VolunteerPreferenceController.class)
@Import(TestJacksonConfig.class)
class VolunteerPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolunteerPreferenceService volunteerPreferenceService;

    @Test
    @DisplayName("Should return preferences by user id")
    void shouldGetPreferencesByUserId() throws Exception {
        var preference = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceService.getPreferencesByUserId(10L)).thenReturn(List.of(preference));

        mockMvc.perform(get("/volunteer/preferences/user/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", equalTo(10)));
    }

    @Test
    @DisplayName("Should return preference by id")
    void shouldGetPreferenceById() throws Exception {
        var preference = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceService.getPreferenceById(1L)).thenReturn(preference);

        mockMvc.perform(get("/volunteer/preferences/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(10)));
    }

    @Test
    @DisplayName("Should return 404 when preference not found")
    void shouldReturn404WhenPreferenceNotFound() throws Exception {
        when(volunteerPreferenceService.getPreferenceById(99L))
                .thenThrow(new VolunteerPreferenceNotFoundException(99L));

        mockMvc.perform(get("/volunteer/preferences/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create a preference and return 201")
    void shouldCreatePreference() throws Exception {
                var request = new CreateVolunteerPreferenceRequest(10L, 1L);
        var saved = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceService.createPreference(any(CreateVolunteerPreferenceRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/volunteer/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", equalTo(10)));
    }

    @Test
    @DisplayName("Should return volunteers with preference flag")
    void shouldCheckVolunteerPreferences() throws Exception {
        var request = new CheckVolunteerPreferencesRequest(1L, List.of(10L, 20L));
        var response = List.of(
                new VolunteerWithPreferenceDTO(10L, true),
                new VolunteerWithPreferenceDTO(20L, false)
        );

        when(volunteerPreferenceService.checkVolunteerPreferences(1L, List.of(10L, 20L)))
                .thenReturn(response);

        mockMvc.perform(post("/volunteer/preferences/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].volunteerId", equalTo(10)))
                .andExpect(jsonPath("$[0].hasPreference", equalTo(true)))
                .andExpect(jsonPath("$[1].volunteerId", equalTo(20)))
                .andExpect(jsonPath("$[1].hasPreference", equalTo(false)));
    }

    @Test
    @DisplayName("Should update a preference")
    void shouldUpdatePreference() throws Exception {
        var request = new CreateVolunteerPreferenceRequest(10L, 1L);
        var updated = VolunteerPreferenceMother.defaultPreference();
        when(volunteerPreferenceService.updatePreference(eq(1L), any(CreateVolunteerPreferenceRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/volunteer/preferences/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTypeId", equalTo(1)));
    }

    @Test
    @DisplayName("Should delete a preference and return 204")
    void shouldDeletePreference() throws Exception {
        doNothing().when(volunteerPreferenceService).deletePreference(1L);

        mockMvc.perform(delete("/volunteer/preferences/1"))
                .andExpect(status().isNoContent());
    }
}
