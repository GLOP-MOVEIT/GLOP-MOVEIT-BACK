package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Status;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.exception.TrialNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.repository.TrialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrialServiceTest {
    @Mock
    private TrialRepository trialRepository;
    @Mock
    private CompetitionRepository competitionRepository;
    @InjectMocks
    private TrialService trialService;

    private Competition competition;
    private Trial trial;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        competition = new Competition();
        competition.setCompetitionId(1);
        competition.setCompetitionName("Compétition Test");
        trial = new Trial();
        trial.setTrialId(1);
        trial.setTrialName("Trial 1");
        trial.setTrialStartDate(new Date());
        trial.setTrialEndDate(new Date());
        trial.setTrialStatus(Status.PLANNED);
        trial.setCompetition(competition);
    }

    @Test
    void getTrialById_shouldReturnTrial_whenExists() {
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        Trial found = trialService.getTrialById(1);
        assertThat(found).isEqualTo(trial);
    }

    @Test
    void getTrialById_shouldThrow_whenNotFound() {
        when(trialRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> trialService.getTrialById(1)).isInstanceOf(TrialNotFoundException.class);
    }

    @Test
    void getTrialsByCompetitionId_shouldReturnList_whenCompetitionExists() {
        when(competitionRepository.existsById(1)).thenReturn(true);
        when(trialRepository.findByCompetition_CompetitionId(1)).thenReturn(List.of(trial));
        List<Trial> trials = trialService.getTrialsByCompetitionId(1);
        assertThat(trials).containsExactly(trial);
    }

    @Test
    void getTrialsByCompetitionId_shouldThrow_whenCompetitionNotFound() {
        when(competitionRepository.existsById(1)).thenReturn(false);
        assertThatThrownBy(() -> trialService.getTrialsByCompetitionId(1)).isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    void createTrial_shouldSaveAndReturnTrial() {
        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));
        when(trialRepository.save(any(Trial.class))).thenReturn(trial);
        Trial created = trialService.createTrial(1, trial);
        assertThat(created).isEqualTo(trial);
        assertThat(created.getCompetition()).isEqualTo(competition);
    }

    @Test
    void createTrial_shouldThrow_whenCompetitionNotFound() {
        when(competitionRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> trialService.createTrial(1, trial)).isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    void updateTrial_shouldUpdateFields() {
        Trial updated = new Trial();
        updated.setTrialName("Updated");
        updated.setTrialStartDate(new Date());
        updated.setTrialEndDate(new Date());
        updated.setTrialDescription("desc");
        updated.setTrialStatus(Status.ONGOING);
        updated.setLocation("Paris");
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(trialRepository.save(any(Trial.class))).thenAnswer(inv -> inv.getArgument(0));
        Trial result = trialService.updateTrial(1, updated);
        assertThat(result.getTrialName()).isEqualTo("Updated");
        assertThat(result.getTrialStatus()).isEqualTo(Status.ONGOING);
        assertThat(result.getLocation()).isEqualTo("Paris");
    }

    @Test
    void updateTrial_shouldThrow_whenNotFound() {
        when(trialRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> trialService.updateTrial(1, trial)).isInstanceOf(TrialNotFoundException.class);
    }

    @Test
    void deleteTrial_shouldDelete_whenExists() {
        when(trialRepository.existsById(1)).thenReturn(true);
        doNothing().when(trialRepository).deleteById(1);
        trialService.deleteTrial(1);
        verify(trialRepository).deleteById(1);
    }

    @Test
    void deleteTrial_shouldThrow_whenNotFound() {
        when(trialRepository.existsById(1)).thenReturn(false);
        assertThatThrownBy(() -> trialService.deleteTrial(1)).isInstanceOf(TrialNotFoundException.class);
    }
}
