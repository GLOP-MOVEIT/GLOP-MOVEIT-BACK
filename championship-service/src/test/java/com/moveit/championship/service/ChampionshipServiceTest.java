package com.moveit.championship.service;

import com.moveit.championship.dto.ChampionshipUpdateDTO;
import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Status;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.mother.ChampionshipMother;
import com.moveit.championship.repository.ChampionshipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChampionshipServiceTest {
    @InjectMocks
    ChampionshipService championshipService;

    @Mock
    ChampionshipRepository championshipRepository;

    @Test
    @DisplayName("Should retrieve all championships.")
    void shouldGetAllChampionships() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.findAll())
                .thenReturn(List.of(championship));

        var championships = championshipService.getAllChampionships();
        assertThat(championships).isEqualTo(List.of(championship));
    }

    @Test
    @DisplayName("Should retrieve championship by id.")
    void shouldGetChampionshipById() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.findById(championship.getId()))
                .thenReturn(Optional.of(championship));

        var result = championshipService.getChampionshipById(championship.getId());
        assertThat(result).isEqualTo(championship);
    }

    @Test
    @DisplayName("Should throw exception when championship not found by id.")
    void shouldThrowExceptionWhenChampionshipNotFound() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.findById(championshipId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> championshipService.getChampionshipById(championshipId))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship with id " + championshipId + " not found");
    }

    @Test
    @DisplayName("Should create championship.")
    void shouldCreateChampionship() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.save(any(Championship.class)))
                .thenReturn(championship);

        var result = championshipService.createChampionship(championship);

        assertThat(result).isEqualTo(championship);
        verify(championshipRepository, times(1)).save(championship);
    }

    @Test
    @DisplayName("Should update championship.")
    void shouldUpdateChampionship() {
        var existingChampionship = ChampionshipMother.championship().build();
        var dto = new ChampionshipUpdateDTO(
                "Updated Championship",
                existingChampionship.getDescription(),
                existingChampionship.getStartDate(),
                existingChampionship.getEndDate(),
                existingChampionship.getStatus()
        );

        when(championshipRepository.findById(existingChampionship.getId()))
                .thenReturn(Optional.of(existingChampionship));
        when(championshipRepository.save(any(Championship.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = championshipService.updateChampionship(existingChampionship.getId(), dto);

        assertThat(result.getName()).isEqualTo("Updated Championship");
        assertThat(result.getId()).isEqualTo(existingChampionship.getId());
        verify(championshipRepository, times(1)).save(any(Championship.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent championship.")
    void shouldThrowExceptionWhenUpdatingNonExistentChampionship() {
        var championship = ChampionshipMother.championship().build();
        var championshipId = championship.getId();
        var dto = new ChampionshipUpdateDTO(
                "Updated",
                "Desc",
                championship.getStartDate(),
                championship.getEndDate(),
                Status.PLANNED
        );

        when(championshipRepository.findById(championshipId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> championshipService.updateChampionship(championshipId, dto))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship with id " + championshipId + " not found");

        verify(championshipRepository, never()).save(any(Championship.class));
    }

    @Test
    @DisplayName("Should delete championship.")
    void shouldDeleteChampionship() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.existsById(championshipId))
                .thenReturn(true);
        doNothing().when(championshipRepository).deleteById(championshipId);

        championshipService.deleteChampionship(championshipId);

        verify(championshipRepository, times(1)).deleteById(championshipId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent championship.")
    void shouldThrowExceptionWhenDeletingNonExistentChampionship() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.existsById(championshipId))
                .thenReturn(false);

        assertThatThrownBy(() -> championshipService.deleteChampionship(championshipId))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship with id " + championshipId + " not found");

        verify(championshipRepository, never()).deleteById(championshipId);
    }
}