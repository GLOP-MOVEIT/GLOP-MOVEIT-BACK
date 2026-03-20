package com.moveit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.moveit.user.dto.Team;
import com.moveit.user.dto.TeamRequest;
import com.moveit.user.dto.User;
import com.moveit.user.exception.GlobalExceptionHandler;
import com.moveit.user.exception.TeamNotFoundException;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        mockMvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testTeam = new Team();
        testTeam.setTeamId(1);
        testTeam.setName("Team Alpha");
        testTeam.setAthletes(List.of());
    }

    // --- getAllTeams ---

    @Test
    void getTeamsByAthleteId_ShouldReturnTeams() throws Exception {
        when(teamService.getTeamsByAthleteId(5)).thenReturn(List.of(testTeam));

        mockMvc.perform(get("/teams/athletes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].name").value("Team Alpha"));

        verify(teamService).getTeamsByAthleteId(5);
    }

    // --- createTeam ---

    @Test
    void createTeam_ShouldReturnCreatedTeam() throws Exception {
        when(teamService.createTeam(any(TeamRequest.class))).thenReturn(testTeam);

        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamRequest("Team Alpha"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId").value(1))
                .andExpect(jsonPath("$.name").value("Team Alpha"))
                .andExpect(jsonPath("$.athletes").isArray())
                .andExpect(jsonPath("$.athletes").isEmpty());

        verify(teamService).createTeam(any(TeamRequest.class));
    }

    @Test
    void createTeam_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamRequest(""))))
                .andExpect(status().isBadRequest());

        verify(teamService, never()).createTeam(any());
    }

    // --- addAthlete ---

    @Test
    void addAthlete_ShouldReturnTeamWithAthlete() throws Exception {
        User athleteDto = new User();
        athleteDto.setUserId(5);
        athleteDto.setFirstName("John");
        athleteDto.setSurname("Doe");

        Team teamWithAthlete = new Team();
        teamWithAthlete.setTeamId(1);
        teamWithAthlete.setName("Team Alpha");
        teamWithAthlete.setAthletes(List.of(athleteDto));

        when(teamService.addAthlete(1, 5)).thenReturn(teamWithAthlete);

        mockMvc.perform(post("/teams/1/athletes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(1))
                .andExpect(jsonPath("$.name").value("Team Alpha"))
                .andExpect(jsonPath("$.athletes").isArray())
                .andExpect(jsonPath("$.athletes[0].userId").value(5));

        verify(teamService).addAthlete(1, 5);
    }

    @Test
    void addAthlete_ShouldReturnNotFound_WhenTeamDoesNotExist() throws Exception {
        when(teamService.addAthlete(999, 1))
                .thenThrow(new TeamNotFoundException("Team with id 999 not found"));

        mockMvc.perform(post("/teams/999/athletes/1"))
                .andExpect(status().isNotFound());

        verify(teamService).addAthlete(999, 1);
    }

    @Test
    void addAthlete_ShouldReturnNotFound_WhenAthleteDoesNotExist() throws Exception {
        when(teamService.addAthlete(1, 999))
                .thenThrow(new UserNotFoundException(999));

        mockMvc.perform(post("/teams/1/athletes/999"))
                .andExpect(status().isNotFound());

        verify(teamService).addAthlete(1, 999);
    }

    // --- removeAthlete ---

    @Test
    void removeAthlete_ShouldReturnTeamWithoutAthlete() throws Exception {
        when(teamService.removeAthlete(1, 5)).thenReturn(testTeam);

        mockMvc.perform(delete("/teams/1/athletes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(1))
                .andExpect(jsonPath("$.athletes").isEmpty());

        verify(teamService).removeAthlete(1, 5);
    }

    @Test
    void removeAthlete_ShouldReturnNotFound_WhenTeamDoesNotExist() throws Exception {
        when(teamService.removeAthlete(999, 1))
                .thenThrow(new TeamNotFoundException("Team with id 999 not found"));

        mockMvc.perform(delete("/teams/999/athletes/1"))
                .andExpect(status().isNotFound());

        verify(teamService).removeAthlete(999, 1);
    }

    @Test
    void removeAthlete_ShouldReturnNotFound_WhenAthleteDoesNotExist() throws Exception {
        when(teamService.removeAthlete(1, 999))
                .thenThrow(new UserNotFoundException(999));

        mockMvc.perform(delete("/teams/1/athletes/999"))
                .andExpect(status().isNotFound());

        verify(teamService).removeAthlete(1, 999);
    }

    // --- deleteTeam ---

    @Test
    void deleteTeam_ShouldReturnNoContent() throws Exception {
        doNothing().when(teamService).deleteTeam(1);

        mockMvc.perform(delete("/teams/1"))
                .andExpect(status().isNoContent());

        verify(teamService).deleteTeam(1);
    }

    @Test
    void deleteTeam_ShouldReturnNotFound_WhenTeamDoesNotExist() throws Exception {
        doThrow(new TeamNotFoundException("Team with id 999 not found"))
                .when(teamService).deleteTeam(999);

        mockMvc.perform(delete("/teams/999"))
                .andExpect(status().isNotFound());

        verify(teamService).deleteTeam(999);
    }
}
