package com.moveit.championship.service;

import com.moveit.championship.entity.Competition;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.mother.CompetitionMother;
import com.moveit.championship.repository.CompetitionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

        var result = competitionService.createCompetition(competition);

        assertThat(result).isEqualTo(competition);
        verify(competitionRepository, times(1)).save(competition);
    }

    @Test
    @DisplayName("Should update competition.")
    void shouldUpdateCompetition() {
        Competition existing = CompetitionMother.competition().build();
        Competition updated = CompetitionMother.competition()
                .withCompetitionName("Updated Competition")
                .build();

        when(competitionRepository.findById(existing.getCompetitionId()))
                .thenReturn(Optional.of(existing));
        when(competitionRepository.save(any(Competition.class))).thenReturn(updated);

        var result = competitionService.updateCompetition(existing.getCompetitionId(), updated);

        assertThat(result).isEqualTo(updated);
        verify(competitionRepository, times(1)).save(any(Competition.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent competition.")
    void shouldThrowExceptionWhenUpdatingNonExistentCompetition() {
        Competition competition = CompetitionMother.competition().build();

        when(competitionRepository.findById(competition.getCompetitionId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionService.updateCompetition(competition.getCompetitionId(), competition))
                .isInstanceOf(CompetitionNotFoundException.class)
                .hasMessageContaining("Competition with id " + competition.getCompetitionId() + " not found");

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
}
