package com.moveit.championship.service;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.mother.ChampionshipMother;
import com.moveit.championship.mother.CompetitionMother;
import com.moveit.championship.repository.ChampionshipRepository;
import com.moveit.championship.repository.CompetitionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceTest {

    @InjectMocks
    private CompetitionService competitionService;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private ChampionshipRepository championshipRepository;

    @Test
    @DisplayName("Should retrieve all competitions.")
    void shouldGetAllCompetitions() {
        Competition competition = CompetitionMother.competition().build();
        when(competitionRepository.findAll()).thenReturn(List.of(competition));

        var result = competitionService.getAllCompetitions();

        assertThat(result).isEqualTo(List.of(competition));
        verify(competitionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve competition by id.")
    void shouldGetCompetitionById() {
        Competition competition = CompetitionMother.competition().build();

        when(competitionRepository.findById(competition.getCompetitionId()))
                .thenReturn(Optional.of(competition));

        var result = competitionService.getCompetitionById(competition.getCompetitionId());

        assertThat(result).isEqualTo(competition);
        verify(competitionRepository, times(1)).findById(competition.getCompetitionId());
    }

    @Test
    @DisplayName("Should throw exception when competition not found by id.")
    void shouldThrowExceptionWhenCompetitionNotFound() {
        Integer id = CompetitionMother.competition().build().getCompetitionId();

        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.getCompetitionById(id))
                .isInstanceOf(CompetitionNotFoundException.class)
                .hasMessageContaining("Competition with id " + id + " not found");
    }

    @Test
    @DisplayName("Should create competition.")
    void shouldCreateCompetition() {
        Competition competition = CompetitionMother.competition().build();
        Championship championship = competition.getChampionship();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));
        when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

        var result = competitionService.createCompetition(competition);

        assertThat(result).isEqualTo(competition);
        verify(championshipRepository, times(1)).findById(championship.getId());
        verify(competitionRepository, times(1)).save(competition);
    }

    @Test
    @DisplayName("Should update competition.")
    void shouldUpdateCompetition() {
        Competition existing = CompetitionMother.competition().build();
        Competition updated = CompetitionMother.competition()
                .withCompetitionName("Updated Competition")
                .build();
        Championship championship = updated.getChampionship();

        when(competitionRepository.findById(existing.getCompetitionId()))
                .thenReturn(Optional.of(existing));
        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));
        when(competitionRepository.save(any(Competition.class))).thenReturn(updated);

        var result = competitionService.updateCompetition(existing.getCompetitionId(), updated);

        assertThat(result).isEqualTo(updated);
        verify(championshipRepository, times(1)).findById(championship.getId());
        verify(competitionRepository, times(1)).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent competition.")
    void shouldThrowExceptionWhenUpdatingNonExistentCompetition() {
        Competition competition = CompetitionMother.competition().build();
        Integer competitionId = competition.getCompetitionId();

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.updateCompetition(competitionId, competition))
                .isInstanceOf(CompetitionNotFoundException.class)
                .hasMessageContaining("Competition with id " + competitionId + " not found");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should delete competition.")
    void shouldDeleteCompetition() {
        Integer id = CompetitionMother.competition().build().getCompetitionId();

        when(competitionRepository.existsById(id)).thenReturn(true);
        doNothing().when(competitionRepository).deleteById(id);

        competitionService.deleteCompetition(id);

        verify(competitionRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent competition.")
    void shouldThrowExceptionWhenDeletingNonExistentCompetition() {
        Integer id = CompetitionMother.competition().build().getCompetitionId();

        when(competitionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> competitionService.deleteCompetition(id))
                .isInstanceOf(CompetitionNotFoundException.class)
                .hasMessageContaining("Competition with id " + id + " not found");

        verify(competitionRepository, never()).deleteById(id);
    }

    @Test
    @DisplayName("Should throw exception when creating competition without championship ID.")
    void shouldThrowExceptionWhenCreatingCompetitionWithoutChampionshipId() {
        Competition competition = CompetitionMother.competition()
                .withChampionship(null)
                .build();

        assertThatThrownBy(() -> competitionService.createCompetition(competition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Championship ID is required");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when creating competition with non-existent championship.")
    void shouldThrowExceptionWhenCreatingCompetitionWithNonExistentChampionship() {
        Competition competition = CompetitionMother.competition().build();
        Championship championship = competition.getChampionship();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.createCompetition(competition))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship with id " + championship.getId() + " not found");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when competition start date is before championship start date.")
    void shouldThrowExceptionWhenCompetitionStartDateIsBeforeChampionshipStartDate() {
        Calendar champStartCal = Calendar.getInstance();
        champStartCal.set(2026, Calendar.FEBRUARY, 1, 0, 0, 0);
        champStartCal.set(Calendar.MILLISECOND, 0);

        Calendar champEndCal = Calendar.getInstance();
        champEndCal.set(2026, Calendar.DECEMBER, 31, 0, 0, 0);
        champEndCal.set(Calendar.MILLISECOND, 0);

        Championship championship = ChampionshipMother.championship()
                .withStartDate(champStartCal.getTime())
                .withEndDate(champEndCal.getTime())
                .build();

        Calendar compStartCal = Calendar.getInstance();
        compStartCal.set(2026, Calendar.JANUARY, 15, 0, 0, 0); // Avant le championship (février)
        compStartCal.set(Calendar.MILLISECOND, 0);

        Calendar compEndCal = Calendar.getInstance();
        compEndCal.set(2026, Calendar.MARCH, 15, 0, 0, 0);
        compEndCal.set(Calendar.MILLISECOND, 0);

        Competition competition = CompetitionMother.competition()
                .withChampionship(championship)
                .withCompetitionStartDate(compStartCal.getTime())
                .withCompetitionEndDate(compEndCal.getTime())
                .build();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));

        assertThatThrownBy(() -> competitionService.createCompetition(competition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La date de début de la compétition doit être après ou égale à la date de début du championnat");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when competition end date is after championship end date.")
    void shouldThrowExceptionWhenCompetitionEndDateIsAfterChampionshipEndDate() {
        Calendar champStartCal = Calendar.getInstance();
        champStartCal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        champStartCal.set(Calendar.MILLISECOND, 0);

        Calendar champEndCal = Calendar.getInstance();
        champEndCal.set(2026, Calendar.APRIL, 30, 0, 0, 0);
        champEndCal.set(Calendar.MILLISECOND, 0);

        Championship championship = ChampionshipMother.championship()
                .withStartDate(champStartCal.getTime())
                .withEndDate(champEndCal.getTime())
                .build();

        Calendar compStartCal = Calendar.getInstance();
        compStartCal.set(2026, Calendar.FEBRUARY, 1, 0, 0, 0);
        compStartCal.set(Calendar.MILLISECOND, 0);

        Calendar compEndCal = Calendar.getInstance();
        compEndCal.set(2026, Calendar.MAY, 31, 0, 0, 0); // Après le championship (avril)
        compEndCal.set(Calendar.MILLISECOND, 0);

        Competition competition = CompetitionMother.competition()
                .withChampionship(championship)
                .withCompetitionStartDate(compStartCal.getTime())
                .withCompetitionEndDate(compEndCal.getTime())
                .build();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));

        assertThatThrownBy(() -> competitionService.createCompetition(competition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La date de fin de la compétition doit être avant ou égale à la date de fin du championnat");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when competition start date is after competition end date.")
    void shouldThrowExceptionWhenCompetitionStartDateIsAfterEndDate() {
        Calendar champStartCal = Calendar.getInstance();
        champStartCal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        champStartCal.set(Calendar.MILLISECOND, 0);

        Calendar champEndCal = Calendar.getInstance();
        champEndCal.set(2026, Calendar.DECEMBER, 31, 0, 0, 0);
        champEndCal.set(Calendar.MILLISECOND, 0);

        Championship championship = ChampionshipMother.championship()
                .withStartDate(champStartCal.getTime())
                .withEndDate(champEndCal.getTime())
                .build();

        Calendar compStartCal = Calendar.getInstance();
        compStartCal.set(2026, Calendar.JUNE, 1, 0, 0, 0);
        compStartCal.set(Calendar.MILLISECOND, 0);

        Calendar compEndCal = Calendar.getInstance();
        compEndCal.set(2026, Calendar.MARCH, 1, 0, 0, 0); // Avant la date de début
        compEndCal.set(Calendar.MILLISECOND, 0);

        Competition competition = CompetitionMother.competition()
                .withChampionship(championship)
                .withCompetitionStartDate(compStartCal.getTime())
                .withCompetitionEndDate(compEndCal.getTime())
                .build();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));

        assertThatThrownBy(() -> competitionService.createCompetition(competition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La date de début de la compétition doit être avant la date de fin");

        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should create competition when dates are valid and within championship date range.")
    void shouldCreateCompetitionWhenDatesAreValid() {
        Calendar champStartCal = Calendar.getInstance();
        champStartCal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        champStartCal.set(Calendar.MILLISECOND, 0);

        Calendar champEndCal = Calendar.getInstance();
        champEndCal.set(2026, Calendar.DECEMBER, 31, 0, 0, 0);
        champEndCal.set(Calendar.MILLISECOND, 0);

        Championship championship = ChampionshipMother.championship()
                .withStartDate(champStartCal.getTime())
                .withEndDate(champEndCal.getTime())
                .build();

        Calendar compStartCal = Calendar.getInstance();
        compStartCal.set(2026, Calendar.MARCH, 1, 0, 0, 0);
        compStartCal.set(Calendar.MILLISECOND, 0);

        Calendar compEndCal = Calendar.getInstance();
        compEndCal.set(2026, Calendar.JUNE, 30, 0, 0, 0);
        compEndCal.set(Calendar.MILLISECOND, 0);

        Competition competition = CompetitionMother.competition()
                .withChampionship(championship)
                .withCompetitionStartDate(compStartCal.getTime())
                .withCompetitionEndDate(compEndCal.getTime())
                .build();

        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));
        when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

        var result = competitionService.createCompetition(competition);

        assertThat(result).isEqualTo(competition);
        verify(championshipRepository, times(1)).findById(championship.getId());
        verify(competitionRepository, times(1)).save(competition);
    }

    // --- assignLocation tests ---

    private Trial createTrial(Integer id, Integer roundNumber) {
        Trial trial = new Trial();
        trial.setTrialId(id);
        trial.setRoundNumber(roundNumber);
        return trial;
    }

    @Test
    @DisplayName("Should assign location to all trials when roundNumber is null")
    void shouldAssignLocationToAllTrials() {
        List<Trial> trials = new ArrayList<>(List.of(
                createTrial(1, 1),
                createTrial(2, 1),
                createTrial(3, 2)
        ));
        Competition competition = CompetitionMother.competition().withTrials(trials).build();

        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

        Competition result = competitionService.assignLocation(1, 5, null);

        assertThat(result.getTrials()).allMatch(t -> t.getLocationId() == 5);
        verify(competitionRepository).save(competition);
    }

    @Test
    @DisplayName("Should assign location only to trials of a specific round")
    void shouldAssignLocationToSpecificRound() {
        Trial t1 = createTrial(1, 1);
        Trial t2 = createTrial(2, 1);
        Trial t3 = createTrial(3, 2);
        List<Trial> trials = new ArrayList<>(List.of(t1, t2, t3));
        Competition competition = CompetitionMother.competition().withTrials(trials).build();

        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

        Competition result = competitionService.assignLocation(1, 7, 1);

        assertThat(result.getTrials().get(0).getLocationId()).isEqualTo(7);
        assertThat(result.getTrials().get(1).getLocationId()).isEqualTo(7);
        assertThat(result.getTrials().get(2).getLocationId()).isNull();
    }

    @Test
    @DisplayName("Should throw when competition not found for assignLocation")
    void shouldThrowWhenCompetitionNotFoundForAssignLocation() {
        when(competitionRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.assignLocation(99, 5, null))
                .isInstanceOf(CompetitionNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when competition has no trials for assignLocation")
    void shouldThrowWhenNoTrialsForAssignLocation() {
        Competition competition = CompetitionMother.competition().withTrials(new ArrayList<>()).build();
        when(competitionRepository.findById(1)).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> competitionService.assignLocation(1, 5, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas encore de matchs");
    }
}
