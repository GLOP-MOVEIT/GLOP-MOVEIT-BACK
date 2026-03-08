package com.moveit.user.service;

import com.moveit.user.dto.Team;
import com.moveit.user.dto.TeamRequest;
import com.moveit.user.dto.User;
import com.moveit.user.entity.TeamEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.TeamNotFoundException;
import com.moveit.user.exception.UserNotFoundException;
import com.moveit.user.mapper.TeamMapper;
import com.moveit.user.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserService userService;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    private TeamEntity testTeamEntity;
    private Team testTeam;
    private UserEntity testAthlete;
    private User testAthleteDto;

    @BeforeEach
    void setUp() {
        testAthlete = new UserEntity();
        testAthlete.setUserId(1);
        testAthlete.setFirstName("John");
        testAthlete.setSurname("Doe");
        testAthlete.setEmail("john.doe@example.com");
        testAthlete.setPhoneNumber("+33123456789");
        testAthlete.setLanguage("FR");

        testAthleteDto = new User();
        testAthleteDto.setUserId(1);
        testAthleteDto.setFirstName("John");
        testAthleteDto.setSurname("Doe");

        testTeamEntity = new TeamEntity();
        testTeamEntity.setTeamId(1);
        testTeamEntity.setName("Team Alpha");
        testTeamEntity.setAthletes(new ArrayList<>());

        testTeam = new Team();
        testTeam.setTeamId(1);
        testTeam.setName("Team Alpha");
        testTeam.setAthletes(List.of());
    }

    // --- createTeam ---

    @Test
    void createTeam_ShouldCreateEmptyTeam() {
        TeamRequest request = new TeamRequest();
        request.setName("Team Alpha");

        TeamEntity savedEntity = new TeamEntity();
        savedEntity.setTeamId(1);
        savedEntity.setName("Team Alpha");
        savedEntity.setAthletes(new ArrayList<>());

        when(teamRepository.save(any(TeamEntity.class))).thenReturn(savedEntity);
        when(teamMapper.toDto(savedEntity)).thenReturn(testTeam);

        Team result = teamService.createTeam(request);

        assertThat(result).isNotNull();
        assertThat(result.getTeamId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Team Alpha");
        assertThat(result.getAthletes()).isEmpty();

        ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Team Alpha");

        verify(teamMapper).toDto(savedEntity);
    }

    // --- addAthlete ---

    @Test
    void addAthlete_ShouldAddAthleteToTeam() {
        Team teamWithAthlete = new Team();
        teamWithAthlete.setTeamId(1);
        teamWithAthlete.setName("Team Alpha");
        teamWithAthlete.setAthletes(List.of(testAthleteDto));

        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));
        when(userService.getUserEntityById(1)).thenReturn(testAthlete);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(testTeamEntity);
        when(teamMapper.toDto(testTeamEntity)).thenReturn(teamWithAthlete);

        Team result = teamService.addAthlete(1, 1);

        assertThat(result).isNotNull();
        assertThat(result.getAthletes()).hasSize(1);
        assertThat(result.getAthletes().getFirst().getUserId()).isEqualTo(1);

        ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getAthletes()).contains(testAthlete);

        verify(teamRepository).findById(1);
        verify(userService).getUserEntityById(1);
    }

    @Test
    void addAthlete_ShouldNotDuplicateAthlete_WhenAlreadyInTeam() {
        testTeamEntity.getAthletes().add(testAthlete);

        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));
        when(userService.getUserEntityById(1)).thenReturn(testAthlete);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(testTeamEntity);
        when(teamMapper.toDto(testTeamEntity)).thenReturn(testTeam);

        teamService.addAthlete(1, 1);

        ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getAthletes()).hasSize(1);
    }

    @Test
    void addAthlete_ShouldThrowTeamNotFoundException_WhenTeamNotFound() {
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addAthlete(999, 1))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team with id 999 not found");

        verify(teamRepository).findById(999);
        verify(userService, never()).getUserEntityById(any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void addAthlete_ShouldThrowUserNotFoundException_WhenAthleteNotFound() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));
        when(userService.getUserEntityById(999)).thenThrow(new UserNotFoundException(999));

        assertThatThrownBy(() -> teamService.addAthlete(1, 999))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(teamRepository).findById(1);
        verify(userService).getUserEntityById(999);
        verify(teamRepository, never()).save(any());
    }

    // --- removeAthlete ---

    @Test
    void removeAthlete_ShouldRemoveAthleteFromTeam() {
        testTeamEntity.getAthletes().add(testAthlete);

        Team emptyTeam = new Team();
        emptyTeam.setTeamId(1);
        emptyTeam.setName("Team Alpha");
        emptyTeam.setAthletes(List.of());

        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));
        when(userService.getUserEntityById(1)).thenReturn(testAthlete);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(testTeamEntity);
        when(teamMapper.toDto(testTeamEntity)).thenReturn(emptyTeam);

        Team result = teamService.removeAthlete(1, 1);

        assertThat(result).isNotNull();
        assertThat(result.getAthletes()).isEmpty();

        ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getAthletes()).doesNotContain(testAthlete);

        verify(teamRepository).findById(1);
        verify(userService).getUserEntityById(1);
    }

    @Test
    void removeAthlete_ShouldThrowTeamNotFoundException_WhenTeamNotFound() {
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.removeAthlete(999, 1))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team with id 999 not found");

        verify(teamRepository).findById(999);
        verify(userService, never()).getUserEntityById(any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void removeAthlete_ShouldThrowUserNotFoundException_WhenAthleteNotFound() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));
        when(userService.getUserEntityById(999)).thenThrow(new UserNotFoundException(999));

        assertThatThrownBy(() -> teamService.removeAthlete(1, 999))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(teamRepository).findById(1);
        verify(userService).getUserEntityById(999);
        verify(teamRepository, never()).save(any());
    }

    // --- deleteTeam ---

    @Test
    void deleteTeam_ShouldDeleteTeam_WhenTeamExists() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeamEntity));

        teamService.deleteTeam(1);

        verify(teamRepository).findById(1);
        verify(teamRepository).delete(testTeamEntity);
    }

    @Test
    void deleteTeam_ShouldThrowTeamNotFoundException_WhenTeamNotFound() {
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(999))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team with id 999 not found");

        verify(teamRepository).findById(999);
        verify(teamRepository, never()).delete(any());
    }
}
