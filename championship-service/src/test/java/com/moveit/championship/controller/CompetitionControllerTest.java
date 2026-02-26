package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Status;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.mother.CompetitionMother;
import com.moveit.championship.service.CompetitionService;
import com.moveit.championship.service.TreeGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompetitionController.class)
@Import(ObjectMapper.class)
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompetitionService competitionService;

    @MockitoBean
    private TreeGenerationService treeGenerationService;

    private Competition competition1;
    private Competition competition2;

    @BeforeEach
    void setUp() {
        competition1 = CompetitionMother.competition().build();
        competition2 = CompetitionMother.competition()
                .withCompetitionId(2)
                .withCompetitionName("Coupe du Monde")
                .build();
    }

    @Test
    @DisplayName("Should retrieve all competitions as summaries (without trials/events).")
    void testGetAllCompetitions_Success() throws Exception {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(competition1, competition2));

        mockMvc.perform(get("/championships/competitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].competitionId", equalTo(competition1.getCompetitionId())))
                .andExpect(jsonPath("$[0].competitionName", equalTo(competition1.getCompetitionName())))
                .andExpect(jsonPath("$[0].trials").doesNotExist())
                .andExpect(jsonPath("$[0].events").doesNotExist())
                .andExpect(jsonPath("$[1].competitionId", equalTo(competition2.getCompetitionId())))
                .andExpect(jsonPath("$[1].competitionName", equalTo(competition2.getCompetitionName())));

        verify(competitionService, times(1)).getAllCompetitions();
    }

    @Test
    @DisplayName("Should retrieve empty list when no competitions exist.")
    void testGetAllCompetitions_Empty() throws Exception {
        when(competitionService.getAllCompetitions()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/championships/competitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(competitionService, times(1)).getAllCompetitions();
    }

    @Test
    @DisplayName("Should retrieve competition by ID successfully.")
    void testGetCompetitionById_Success() throws Exception {
        when(competitionService.getCompetitionById(competition1.getCompetitionId())).thenReturn(competition1);

        mockMvc.perform(get("/championships/competitions/" + competition1.getCompetitionId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionId", equalTo(competition1.getCompetitionId())))
                .andExpect(jsonPath("$.competitionName", equalTo(competition1.getCompetitionName())))
                .andExpect(jsonPath("$.competitionStatus", equalTo(competition1.getCompetitionStatus().name())));

        verify(competitionService, times(1)).getCompetitionById(competition1.getCompetitionId());
    }

    @Test
    @DisplayName("Should return 404 when competition by ID not found.")
    void testGetCompetitionById_NotFound() throws Exception {
        Integer id = 999;
        when(competitionService.getCompetitionById(id)).thenThrow(new CompetitionNotFoundException(id));

        mockMvc.perform(get("/championships/competitions/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(competitionService, times(1)).getCompetitionById(id);
    }

    @Test
    @DisplayName("Should create competition successfully.")
    void testCreateCompetition_Success() throws Exception {
        when(competitionService.createCompetition(any(Competition.class))).thenReturn(competition1);

        mockMvc.perform(post("/championships/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(competition1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.competitionId", equalTo(competition1.getCompetitionId())))
                .andExpect(jsonPath("$.competitionName", equalTo(competition1.getCompetitionName())));

        verify(competitionService, times(1)).createCompetition(any(Competition.class));
    }

    @Test
    @DisplayName("Should update competition successfully.")
    void testUpdateCompetition_Success() throws Exception {
        var updateDto = com.moveit.championship.dto.CompetitionUpdateDTOMother.validUpdate();
        Competition updatedCompetition = CompetitionMother.competition()
                .withCompetitionId(competition1.getCompetitionId())
                .withCompetitionName(updateDto.getCompetitionName())
                .withCompetitionStatus(updateDto.getCompetitionStatus())
                .build();

        when(competitionService.updateCompetition(eq(competition1.getCompetitionId()), any(com.moveit.championship.dto.CompetitionUpdateDTO.class)))
                .thenReturn(updatedCompetition);

        mockMvc.perform(put("/championships/competitions/" + competition1.getCompetitionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionId", equalTo(updatedCompetition.getCompetitionId())))
                .andExpect(jsonPath("$.competitionName", equalTo(updatedCompetition.getCompetitionName())))
                .andExpect(jsonPath("$.competitionStatus", equalTo(updatedCompetition.getCompetitionStatus().name())));

        verify(competitionService, times(1)).updateCompetition(eq(competition1.getCompetitionId()), any(com.moveit.championship.dto.CompetitionUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent competition.")
    void testUpdateCompetition_NotFound() throws Exception {
        Integer id = 999;
        var updateDto = com.moveit.championship.dto.CompetitionUpdateDTOMother.validUpdate();

        when(competitionService.updateCompetition(eq(id), any(com.moveit.championship.dto.CompetitionUpdateDTO.class)))
                .thenThrow(new CompetitionNotFoundException(id));

        mockMvc.perform(put("/championships/competitions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(competitionService, times(1)).updateCompetition(eq(id), any(com.moveit.championship.dto.CompetitionUpdateDTO.class));
    }

    @Test
    @DisplayName("Should generate tree successfully.")
    void testGenerateTree_Success() throws Exception {
        Integer id = competition1.getCompetitionId();
        List<Integer> participantIds = List.of(1, 2, 3, 4);

        when(treeGenerationService.generateTree(eq(id), anyList())).thenReturn(competition1);

        mockMvc.perform(post("/championships/competitions/" + id + "/generate-tree")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionId", equalTo(competition1.getCompetitionId())))
                .andExpect(jsonPath("$.competitionName", equalTo(competition1.getCompetitionName())));

        verify(treeGenerationService, times(1)).generateTree(eq(id), anyList());
    }

    @Test
    @DisplayName("Should return 404 when generating tree for non-existent competition.")
    void testGenerateTree_NotFound() throws Exception {
        Integer id = 999;
        List<Integer> participantIds = List.of(1, 2);

        when(treeGenerationService.generateTree(eq(id), anyList()))
                .thenThrow(new CompetitionNotFoundException(id));

        mockMvc.perform(post("/championships/competitions/" + id + "/generate-tree")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantIds)))
                .andExpect(status().isNotFound());

        verify(treeGenerationService, times(1)).generateTree(eq(id), anyList());
    }

    @Test
    @DisplayName("Should generate tree even without extra params.")
    void testGenerateTree_NoExtraParam() throws Exception {
        Integer id = competition1.getCompetitionId();
        List<Integer> participantIds = List.of(1, 2);

        when(treeGenerationService.generateTree(eq(id), anyList())).thenReturn(competition1);

        mockMvc.perform(post("/championships/competitions/" + id + "/generate-tree")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantIds)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should delete competition successfully.")
    void testDeleteCompetition_Success() throws Exception {
        Integer id = competition1.getCompetitionId();
        doNothing().when(competitionService).deleteCompetition(id);

        mockMvc.perform(delete("/championships/competitions/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(competitionService, times(1)).deleteCompetition(id);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent competition.")
    void testDeleteCompetition_NotFound() throws Exception {
        Integer id = 999;
        doThrow(new CompetitionNotFoundException(id)).when(competitionService).deleteCompetition(id);

        mockMvc.perform(delete("/championships/competitions/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(competitionService, times(1)).deleteCompetition(id);
    }

    // --- assignLocation tests ---

    @Test
    @DisplayName("Should assign location to all trials successfully.")
    void testAssignLocation_Success() throws Exception {
        Integer id = competition1.getCompetitionId();
        when(competitionService.assignLocation(eq(id), eq(5), isNull())).thenReturn(competition1);

        String body = "{\"locationId\": 5}";

        mockMvc.perform(put("/championships/competitions/" + id + "/assign-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionId", equalTo(id)));

        verify(competitionService, times(1)).assignLocation(eq(id), eq(5), isNull());
    }

    @Test
    @DisplayName("Should assign location to specific round successfully.")
    void testAssignLocation_WithRoundNumber() throws Exception {
        Integer id = competition1.getCompetitionId();
        when(competitionService.assignLocation(eq(id), eq(7), eq(2))).thenReturn(competition1);

        String body = "{\"locationId\": 7, \"roundNumber\": 2}";

        mockMvc.perform(put("/championships/competitions/" + id + "/assign-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionId", equalTo(id)));

        verify(competitionService, times(1)).assignLocation(eq(id), eq(7), eq(2));
    }

    @Test
    @DisplayName("Should return 404 when assigning location to non-existent competition.")
    void testAssignLocation_NotFound() throws Exception {
        Integer id = 999;
        when(competitionService.assignLocation(eq(id), eq(5), isNull()))
                .thenThrow(new CompetitionNotFoundException(id));

        String body = "{\"locationId\": 5}";

        mockMvc.perform(put("/championships/competitions/" + id + "/assign-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(competitionService, times(1)).assignLocation(eq(id), eq(5), isNull());
    }

    @Test
    @DisplayName("Should return 400 when locationId is missing.")
    void testAssignLocation_MissingLocationId() throws Exception {
        Integer id = competition1.getCompetitionId();

        String body = "{\"roundNumber\": 1}";

        mockMvc.perform(put("/championships/competitions/" + id + "/assign-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(competitionService, never()).assignLocation(any(), any(), any());
    }
}
