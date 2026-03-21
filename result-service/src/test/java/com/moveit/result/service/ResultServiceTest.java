package com.moveit.result.service;

import com.moveit.result.dto.Result;
import com.moveit.result.entity.ResultEntity;
import com.moveit.result.exception.ResultNotFoundException;
import com.moveit.result.mapper.ResultMapper;
import com.moveit.result.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    private Result dto;
    private ResultEntity entity;

    @BeforeEach
    void setUp() {
        dto = new Result();
        dto.setTrialId(123);
        dto.setLastTrial(true);
        // ...initialiser d'autres champs utiles du DTO (rankings, participants, etc.) si disponibles...

        entity = new ResultEntity();
        entity.setTrialId(123);
        entity.setLastTrial(false);
        // ...initialiser d'autres champs utiles de l'entité...
    }

    @Test
    void saveResult_shouldMapAndSaveAndReturnDto_whenResultIsValid() {
        // Given
        given(resultMapper.toEntity(dto)).willReturn(entity);
        given(resultRepository.save(entity)).willReturn(entity);
        given(resultMapper.toDto(entity)).willReturn(dto);

        // When
        Result result = resultService.saveResult(dto);

        // Then
        assertThat(result).isEqualTo(dto);
        verify(resultMapper).toEntity(dto);
        verify(resultRepository).save(entity);
        verify(resultMapper).toDto(entity);
        verifyNoMoreInteractions(resultRepository, resultMapper);
    }

    @Test
    void getResultByTrialId_shouldReturnDto_whenResultExists() {
        // Given
        Integer trialId = 123;
        given(resultRepository.findByTrialId(trialId)).willReturn(Optional.of(entity));
        given(resultMapper.toDto(entity)).willReturn(dto);

        // When
        Result result = resultService.getResultByTrialId(trialId);

        // Then
        assertThat(result).isEqualTo(dto);
        verify(resultRepository).findByTrialId(trialId);
        verify(resultMapper).toDto(entity);
        verifyNoMoreInteractions(resultRepository, resultMapper);
    }

    @Test
    void getResultByTrialId_shouldThrowResultNotFoundException_whenResultDoesNotExist() {
        // Given
        Integer trialId = 999;
        given(resultRepository.findByTrialId(trialId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> resultService.getResultByTrialId(trialId))
                .isInstanceOf(ResultNotFoundException.class)
                .hasMessageContaining(String.valueOf(trialId));

        verify(resultRepository).findByTrialId(trialId);
        verifyNoMoreInteractions(resultRepository);
        verifyNoInteractions(resultMapper);
    }

    @Test
    void updateResult_shouldUpdateAndReturnDto_whenExistingResultFound() {
        // Given
        dto.setLastTrial(true);
        // ...initialiser d'autres champs du DTO comme rankings...

        ResultEntity existingEntity = new ResultEntity();
        existingEntity.setTrialId(123);
        existingEntity.setLastTrial(false);
        // ...initialiser d'autres champs de l'entité existante...

        given(resultRepository.findByTrialId(dto.getTrialId())).willReturn(Optional.of(existingEntity));
        given(resultRepository.save(any(ResultEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(resultMapper.toDto(any(ResultEntity.class))).willReturn(dto);

        // When
        Result result = resultService.updateResult(dto);

        // Then
        assertThat(result).isEqualTo(dto);

        ArgumentCaptor<ResultEntity> captor = ArgumentCaptor.forClass(ResultEntity.class);
        verify(resultRepository).findByTrialId(dto.getTrialId());
        verify(resultRepository).save(captor.capture());
        verify(resultMapper).toDto(captor.getValue());

        ResultEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getTrialId()).isEqualTo(dto.getTrialId());
        assertThat(savedEntity.isLastTrial()).isEqualTo(Boolean.TRUE.equals(dto.getLastTrial()));
        // ...vérifier d'autres champs comme rankings si disponibles...

        verifyNoMoreInteractions(resultRepository, resultMapper);
    }

    @Test
    void updateResult_shouldThrowResultNotFoundException_whenExistingResultNotFound() {
        // Given
        dto.setTrialId(456);
        given(resultRepository.findByTrialId(dto.getTrialId())).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> resultService.updateResult(dto))
                .isInstanceOf(ResultNotFoundException.class)
                .hasMessageContaining(String.valueOf(dto.getTrialId()));

        verify(resultRepository).findByTrialId(dto.getTrialId());
        verifyNoMoreInteractions(resultRepository);
        verifyNoInteractions(resultMapper);
    }

    @Test
    void getAllResultsByParticipantId_shouldReturnMappedList_whenResultsExist() {
        // Given
        Integer participantId = 42;
        ResultEntity entity1 = new ResultEntity();
        entity1.setTrialId(1);
        ResultEntity entity2 = new ResultEntity();
        entity2.setTrialId(2);

        Result dto1 = new Result();
        dto1.setTrialId(1);
        Result dto2 = new Result();
        dto2.setTrialId(2);

        given(resultRepository.findByParticipantsId(participantId)).willReturn(List.of(entity1, entity2));
        given(resultMapper.toDto(entity1)).willReturn(dto1);
        given(resultMapper.toDto(entity2)).willReturn(dto2);

        // When
        List<Result> results = resultService.getAllResultsByParticipantId(participantId);

        // Then
        assertThat(results).containsExactly(dto1, dto2);
        verify(resultRepository).findByParticipantsId(participantId);
        verify(resultMapper).toDto(entity1);
        verify(resultMapper).toDto(entity2);
        verifyNoMoreInteractions(resultRepository, resultMapper);
    }

    @Test
    void getAllResultsByParticipantId_shouldReturnEmptyList_whenNoResultsFound() {
        // Given
        Integer participantId = 42;
        given(resultRepository.findByParticipantsId(participantId)).willReturn(List.of());

        // When
        List<Result> results = resultService.getAllResultsByParticipantId(participantId);

        // Then
        assertThat(results).isEmpty();
        verify(resultRepository).findByParticipantsId(participantId);
        verifyNoMoreInteractions(resultRepository);
        verifyNoInteractions(resultMapper);
    }
}