package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.service.TrialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrialController.class)
@Import(ObjectMapper.class)
class TrialControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private TrialService trialService;

    private Trial trial;

    @BeforeEach
    void setUp() {
        trial = new Trial();
        trial.setTrialId(1);
        trial.setTrialName("Trial 1");
        trial.setTrialStartDate(new Date());
        trial.setTrialEndDate(new Date());
        trial.setTrialStatus(Status.PLANNED);
        trial.setLocation("Paris");
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
    void deleteTrial_shouldReturnNoContent() throws Exception {
        doNothing().when(trialService).deleteTrial(1);
        mockMvc.perform(delete("/trials/1"))
                .andExpect(status().isNoContent());
    }
}
