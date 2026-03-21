package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.championship.config.TestJacksonConfig;
import com.moveit.championship.dto.ParticipantDTO;
import com.moveit.championship.dto.TrialDTO;
import com.moveit.championship.dto.TrialRequestDTO;
import com.moveit.championship.dto.TrialWithParticipantsDTO;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.mapper.TrialMapper;
import com.moveit.championship.service.TrialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TrialController.class)
@Import(TestJacksonConfig.class)
class TrialControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private TrialService trialService;

    @MockitoBean
    private TrialMapper trialMapper;

    private Trial trial;

    @BeforeEach
    void setUp() {
        trial = new Trial();
        trial.setTrialId(1);
        trial.setTrialName("Trial 1");
        trial.setTrialStartDate(LocalDateTime.now());
        trial.setTrialEndDate(LocalDateTime.now());
        trial.setTrialStatus(Status.PLANNED);
        trial.setLocationId(1);
        trial.setParticipantIds(List.of(7, 8));

        when(trialMapper.toTrialDTO(any(Trial.class))).thenAnswer(invocation -> {
            Trial source = invocation.getArgument(0);
            return TrialDTO.builder()
                    .trialId(source.getTrialId())
                    .trialName(source.getTrialName())
                    .trialStartDate(source.getTrialStartDate())
                    .trialEndDate(source.getTrialEndDate())
                    .trialDescription(source.getTrialDescription())
                    .trialStatus(source.getTrialStatus())
                    .locationId(source.getLocationId())
                    .roundNumber(source.getRoundNumber())
                    .position(source.getPosition())
                    .participantIds(source.getParticipantIds())
                    .build();
        });

        when(trialMapper.toTrialEntity(any(TrialRequestDTO.class))).thenAnswer(invocation -> {
            TrialRequestDTO source = invocation.getArgument(0);
            Trial mapped = new Trial();
            mapped.setTrialName(source.getTrialName());
            mapped.setTrialStartDate(source.getTrialStartDate());
            mapped.setTrialEndDate(source.getTrialEndDate());
            mapped.setTrialDescription(source.getTrialDescription());
            mapped.setTrialStatus(source.getTrialStatus());
            mapped.setLocationId(source.getLocationId());
            mapped.setRoundNumber(source.getRoundNumber());
            mapped.setPosition(source.getPosition());
            mapped.setParticipantIds(source.getParticipantIds());
            return mapped;
        });
    }

    @Test
    void getTrialById_shouldReturnTrial() throws Exception {
        when(trialService.getTrialById(1)).thenReturn(trial);
        mockMvc.perform(get("/trials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(1))
                .andExpect(jsonPath("$.trialName").value("Trial 1"));
    }

    @Test
    void getTrialsByAthleteId_shouldReturnList() throws Exception {
        when(trialService.getTrialsByAthleteId(7)).thenReturn(List.of(trial));
        mockMvc.perform(get("/trials/athlete/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trialId").value(1))
                .andExpect(jsonPath("$[0].participantIds[0]").value(7));
    }

    @Test
    void getTrialsByCompetitionId_shouldReturnList() throws Exception {
        when(trialService.getTrialsByCompetitionId(1)).thenReturn(List.of(trial));
        mockMvc.perform(get("/trials/competition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trialId").value(1));
    }

    @Test
    void createTrial_shouldReturnCreated() throws Exception {
        when(trialService.createTrial(eq(1), any(Trial.class))).thenReturn(trial);
        mockMvc.perform(post("/trials/competition/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trialId").value(1));
    }

    @Test
    void updateTrial_shouldReturnUpdated() throws Exception {
        when(trialService.updateTrial(eq(1), any(Trial.class))).thenReturn(trial);
        mockMvc.perform(put("/trials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(1));
    }

    @Test
    void getTrialWithParticipants_shouldReturnEnrichedTrial() throws Exception {
        TrialWithParticipantsDTO enriched = TrialWithParticipantsDTO.builder()
                .trialId(1)
                .trialName("Trial 1")
                .competitionId(3)
                .participants(List.of(
                        new ParticipantDTO(7, "Team Alpha"),
                        new ParticipantDTO(8, "Team Beta")
                ))
                .build();

        when(trialService.getTrialWithParticipants(1)).thenReturn(enriched);

        mockMvc.perform(get("/trials/1/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(1))
                .andExpect(jsonPath("$.competitionId").value(3))
                .andExpect(jsonPath("$.participants[0].id").value(7))
                .andExpect(jsonPath("$.participants[0].name").value("Team Alpha"));
    }

    @Test
    void advanceQualifiedParticipants_shouldReturnUpdatedNextTrial() throws Exception {
        Trial nextTrial = new Trial();
        nextTrial.setTrialId(2);
        nextTrial.setTrialName("Demi-finale - Match 1");
        nextTrial.setTrialStartDate(LocalDateTime.now());
        nextTrial.setTrialEndDate(LocalDateTime.now().plusHours(1));
        nextTrial.setTrialStatus(Status.PLANNED);
        nextTrial.setParticipantIds(List.of(30, 7, 8));

        when(trialService.advanceParticipantsToNextTrial(1, List.of(7, 8))).thenReturn(nextTrial);

        mockMvc.perform(post("/trials/1/advance-qualified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(7, 8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialId").value(2))
                .andExpect(jsonPath("$.participantIds[0]").value(30))
                .andExpect(jsonPath("$.participantIds[1]").value(7))
                .andExpect(jsonPath("$.participantIds[2]").value(8));
    }

    @Test
    void deleteTrial_shouldReturnNoContent() throws Exception {
        doNothing().when(trialService).deleteTrial(1);
        mockMvc.perform(delete("/trials/1"))
                .andExpect(status().isNoContent());
    }
}
