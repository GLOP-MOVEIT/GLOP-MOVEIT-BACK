package com.moveit.championship.service;

import java.util.*;
import java.time.LocalDateTime;

import com.moveit.championship.client.UserClient;
import com.moveit.championship.dto.TeamResponseDTO;
import com.moveit.championship.dto.TrialWithParticipantsDTO;
import com.moveit.championship.dto.UserResponseDTO;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.ParticipantType;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrialServiceTest {
    @Mock
    private TrialRepository trialRepository;
    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private UserClient userClient;
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
        competition.setParticipantType(ParticipantType.INDIVIDUAL);

        trial = new Trial();
        trial.setTrialId(1);
        trial.setTrialName("Trial 1");
        trial.setTrialStartDate(LocalDateTime.now());
        trial.setTrialEndDate(LocalDateTime.now());
        trial.setTrialStatus(Status.PLANNED);
        trial.setCompetition(competition);
        trial.setLocationId(1);
        trial.setParticipantIds(List.of(7, 8));
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
    void getTrialsByAthleteId_shouldReturnTrialsForAthleteAndTeams() {
        when(userClient.getTeamsByAthleteId(7)).thenReturn(List.of(
                new TeamResponseDTO(10, "Team A"),
                new TeamResponseDTO(11, "Team B")
        ));
        when(trialRepository.findByParticipantIds(List.of(7, 10, 11))).thenReturn(List.of(trial));

        List<Trial> trials = trialService.getTrialsByAthleteId(7);

        assertThat(trials).containsExactly(trial);
        verify(trialRepository).findByParticipantIds(List.of(7, 10, 11));
    }

    @Test
    void getTrialsByAthleteId_shouldFallbackToAthleteOnly_whenTeamLookupFails() {
        when(userClient.getTeamsByAthleteId(7)).thenThrow(new RuntimeException("user-service down"));
        when(trialRepository.findByParticipantIds(List.of(7))).thenReturn(List.of(trial));

        List<Trial> trials = trialService.getTrialsByAthleteId(7);

        assertThat(trials).containsExactly(trial);
        verify(trialRepository).findByParticipantIds(List.of(7));
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
        LocalDateTime start = LocalDateTime.of(2026, 3, 24, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 24, 16, 0);
        updated.setTrialStartDate(start);
        updated.setTrialEndDate(end);
        updated.setTrialDescription("desc");
        updated.setTrialStatus(Status.ONGOING);
        updated.setLocationId(2);
        updated.setParticipantIds(List.of(10, 11));

        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(trialRepository.findConflictingTrials(1, List.of(10, 11), start, end)).thenReturn(List.of());
        when(trialRepository.save(any(Trial.class))).thenAnswer(inv -> inv.getArgument(0));

        Trial result = trialService.updateTrial(1, updated);

        assertThat(result.getTrialName()).isEqualTo("Updated");
        assertThat(result.getTrialStatus()).isEqualTo(Status.ONGOING);
        assertThat(result.getLocationId()).isEqualTo(2);
        assertThat(result.getParticipantIds()).containsExactly(10, 11);
    }

    @Test
    void updateTrial_shouldThrow_whenScheduleConflictsForSharedAthlete() {
        LocalDateTime candidateStart = LocalDateTime.of(2026, 3, 24, 15, 0);
        LocalDateTime candidateEnd = LocalDateTime.of(2026, 3, 24, 16, 0);

        Trial updated = new Trial();
        updated.setTrialName("Updated");
        updated.setTrialStartDate(candidateStart);
        updated.setTrialEndDate(candidateEnd);
        updated.setParticipantIds(List.of(7, 8));

        Trial conflicting = new Trial();
        conflicting.setTrialId(2);
        conflicting.setTrialStartDate(LocalDateTime.of(2026, 3, 24, 15, 30));
        conflicting.setTrialEndDate(LocalDateTime.of(2026, 3, 24, 16, 30));
        conflicting.setParticipantIds(List.of(7, 99));

        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(trialRepository.findConflictingTrials(1, List.of(7, 8), candidateStart, candidateEnd))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> trialService.updateTrial(1, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conflit d'emploi du temps")
                .hasMessageContaining("[7]")
                .hasMessageContaining("[2]");

        verify(trialRepository, never()).save(any(Trial.class));
    }

    @Test
    void updateTrial_shouldAllow_whenSharedAthleteHasAdjacentButNonOverlappingTrial() {
        LocalDateTime candidateStart = LocalDateTime.of(2026, 3, 24, 15, 0);
        LocalDateTime candidateEnd = LocalDateTime.of(2026, 3, 24, 16, 0);

        Trial updated = new Trial();
        updated.setTrialName("Updated");
        updated.setTrialStartDate(candidateStart);
        updated.setTrialEndDate(candidateEnd);
        updated.setParticipantIds(List.of(7, 8));

        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(trialRepository.findConflictingTrials(1, List.of(7, 8), candidateStart, candidateEnd))
                .thenReturn(List.of());
        when(trialRepository.save(any(Trial.class))).thenAnswer(inv -> inv.getArgument(0));

        Trial result = trialService.updateTrial(1, updated);

        assertThat(result.getTrialStartDate()).isEqualTo(candidateStart);
        assertThat(result.getTrialEndDate()).isEqualTo(candidateEnd);
        verify(trialRepository).save(any(Trial.class));
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

    @Test
    void getTrialWithParticipants_shouldResolveIndividualParticipants() {
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(userClient.getUserById(7)).thenReturn(new UserResponseDTO(7, "Alice", "Martin"));
        when(userClient.getUserById(8)).thenReturn(new UserResponseDTO(8, "Bob", "Durand"));

        TrialWithParticipantsDTO result = trialService.getTrialWithParticipants(1);

        assertThat(result.getTrialId()).isEqualTo(1);
        assertThat(result.getCompetitionId()).isEqualTo(1);
        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getParticipants().get(0).getName()).isEqualTo("Alice Martin");
        assertThat(result.getParticipants().get(1).getName()).isEqualTo("Bob Durand");
    }

    @Test
    void getTrialWithParticipants_shouldResolveTeamParticipants() {
        competition.setParticipantType(ParticipantType.TEAM);
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(userClient.getTeamById(7)).thenReturn(new TeamResponseDTO(7, "Team Alpha"));
        when(userClient.getTeamById(8)).thenReturn(new TeamResponseDTO(8, "Team Beta"));

        TrialWithParticipantsDTO result = trialService.getTrialWithParticipants(1);

        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getParticipants().get(0).getName()).isEqualTo("Team Alpha");
        assertThat(result.getParticipants().get(1).getName()).isEqualTo("Team Beta");
    }

    @Test
    void getTrialWithParticipants_shouldFallbackToUnknown_whenUserServiceFails() {
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(userClient.getUserById(7)).thenThrow(new RuntimeException("upstream error"));
        when(userClient.getUserById(8)).thenReturn(new UserResponseDTO(8, "Bob", "Durand"));

        TrialWithParticipantsDTO result = trialService.getTrialWithParticipants(1);

        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getParticipants().get(0).getId()).isEqualTo(7);
        assertThat(result.getParticipants().get(0).getName()).isEqualTo("Unknown");
        assertThat(result.getParticipants().get(1).getName()).isEqualTo("Bob Durand");
    }

    @Test
    void advanceParticipantsToNextTrial_shouldAppendQualifiedParticipantsWithoutDuplicates() {
        competition.setCompetitionType(CompetitionType.HEATS);
        competition.setMaxPerHeat(4);

        Trial nextTrial = new Trial();
        nextTrial.setTrialId(2);
        nextTrial.setCompetition(competition);
        nextTrial.setParticipantIds(new ArrayList<>(List.of(30)));
        trial.setNextTrial(nextTrial);

        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        when(trialRepository.save(any(Trial.class))).thenAnswer(inv -> inv.getArgument(0));

        Trial updated = trialService.advanceParticipantsToNextTrial(1, Arrays.asList(7, 8, 8, null));

        assertThat(updated.getTrialId()).isEqualTo(2);
        assertThat(updated.getParticipantIds()).containsExactly(30, 7, 8);
    }

    @Test
    void advanceParticipantsToNextTrial_shouldThrowWhenNextTrialCapacityExceeded() {
        Trial nextTrial = new Trial();
        nextTrial.setTrialId(2);
        nextTrial.setParticipantIds(new ArrayList<>(List.of(30)));
        trial.setNextTrial(nextTrial);

        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        List<Integer> qualifiedIds = List.of(7, 8);

        assertThatThrownBy(() -> trialService.advanceParticipantsToNextTrial(1, qualifiedIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne peut pas depasser 2 participants");
    }

    @Test
    void advanceParticipantsToNextTrial_shouldThrowWhenNoNextTrial() {
        trial.setNextTrial(null);
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        List<Integer> qualifiedIds = List.of(7);

        assertThatThrownBy(() -> trialService.advanceParticipantsToNextTrial(1, qualifiedIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aucune manche suivante");
    }

    @Test
    void advanceParticipantsToNextTrial_shouldThrowWhenQualifiedListIsEmpty() {
        Trial nextTrial = new Trial();
        nextTrial.setTrialId(2);
        trial.setNextTrial(nextTrial);
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial));
        List<Integer> qualifiedIds = List.of();

        assertThatThrownBy(() -> trialService.advanceParticipantsToNextTrial(1, qualifiedIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("liste des qualifies est obligatoire");
    }
}
