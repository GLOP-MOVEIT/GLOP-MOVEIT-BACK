package com.moveit.result.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.result.dto.Ranking;
import com.moveit.result.dto.Result;
import com.moveit.result.exception.ResultNotFoundException;
import com.moveit.result.service.ResultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private ResultService resultService;

    @InjectMocks
    private ResultController resultController;

    private void setupMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.standaloneSetup(resultController).build();
        }
    }

    private Result buildResult(Integer trialId) {
        Result result = new Result();
        result.setResultId(1);
        result.setTrialId(trialId);
        result.setLastTrial(true);
        Ranking ranking = new Ranking();
        ranking.setId(1);
        ranking.setPosition(1);
        result.setRankings(List.of(ranking));
        return result;
    }

    @Test
    @DisplayName("GET /results/trial/{trialId} - 200 OK")
    void getResultByTrialId_shouldReturn200AndBody_whenResultExists() throws Exception {
        setupMockMvc();
        // Given
        Integer trialId = 123;
        Result result = buildResult(trialId);
        given(resultService.getResultByTrialId(trialId)).willReturn(result);

        // When / Then
        mockMvc.perform(get("/results/trial/{trialId}", trialId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trialId").value(trialId));

        verify(resultService).getResultByTrialId(trialId);
    }

    @Test
    @DisplayName("GET /results/trial/{trialId} - exception ResultNotFoundException propagée")
    void getResultByTrialId_shouldPropagateResultNotFoundException_whenResultNotFound() {
        setupMockMvc();
        // Given
        Integer trialId = 999;
        given(resultService.getResultByTrialId(trialId)).willThrow(new ResultNotFoundException("Result not found"));

        // When / Then
        assertThatThrownBy(() -> mockMvc.perform(get("/results/trial/{trialId}", trialId)).andReturn())
                .hasCauseInstanceOf(ResultNotFoundException.class);
    }

    @Test
    @DisplayName("POST /results - 200 OK")
    void saveResult_shouldReturn200AndBody_whenRequestIsValid() throws Exception {
        setupMockMvc();
        // Given
        Result requestDto = buildResult(123);
        Result responseDto = buildResult(123);
        given(resultService.saveResult(any(Result.class))).willReturn(responseDto);

        // When / Then
        mockMvc.perform(post("/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trialId").value(123));

        verify(resultService).saveResult(any(Result.class));
    }

    @Test
    @DisplayName("PUT /results - 200 OK")
    void updateResult_shouldReturn200AndBody_whenRequestIsValid() throws Exception {
        setupMockMvc();
        // Given
        Result requestDto = buildResult(123);
        Result responseDto = buildResult(123);
        given(resultService.updateResult(any(Result.class))).willReturn(responseDto);

        // When / Then
        mockMvc.perform(put("/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trialId").value(123));

        verify(resultService).updateResult(any(Result.class));
    }

    @Test
    @DisplayName("PUT /results - exception ResultNotFoundException propagée")
    void updateResult_shouldPropagateResultNotFoundException_whenResultNotFound() {
        setupMockMvc();
        // Given
        Result requestDto = buildResult(999);
        given(resultService.updateResult(any(Result.class))).willThrow(new ResultNotFoundException("Result not found"));

        // When / Then
        assertThatThrownBy(() -> mockMvc.perform(put("/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                        .andReturn())
                .hasCauseInstanceOf(ResultNotFoundException.class);
    }

    @Test
    @DisplayName("GET /results/participant/{participantId} - 200 OK with list")
    void getAllResultsByParticipantId_shouldReturn200AndList_whenResultsExist() throws Exception {
        setupMockMvc();
        // Given
        Integer participantId = 42;
        List<Result> results = List.of(buildResult(1), buildResult(2));
        given(resultService.getAllResultsByParticipantId(participantId)).willReturn(results);

        // When / Then
        mockMvc.perform(get("/results/participant/{participantId}", participantId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()" ).value(2));

        verify(resultService).getAllResultsByParticipantId(participantId);
    }

    @Test
    @DisplayName("GET /results/participant/{participantId} - 200 OK with empty list")
    void getAllResultsByParticipantId_shouldReturn200AndEmptyList_whenNoResults() throws Exception {
        setupMockMvc();
        // Given
        Integer participantId = 42;
        given(resultService.getAllResultsByParticipantId(participantId)).willReturn(List.of());

        // When / Then
        mockMvc.perform(get("/results/participant/{participantId}", participantId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()" ).value(0));

        verify(resultService).getAllResultsByParticipantId(participantId);
    }
}

