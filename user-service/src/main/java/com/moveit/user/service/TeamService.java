package com.moveit.user.service;

import com.moveit.user.dto.Team;
import com.moveit.user.dto.TeamRequest;
import com.moveit.user.entity.TeamEntity;
import com.moveit.user.entity.UserEntity;
import com.moveit.user.exception.TeamNotFoundException;
import com.moveit.user.exception.UserNotAthleteException;
import com.moveit.user.mapper.TeamMapper;
import com.moveit.user.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TeamMapper teamMapper;

    public Page<Team> getAllTeams(Pageable pageable) {
        return this.teamRepository.findAll(pageable)
                .map(this.teamMapper::toDto);
    }

    public Team createTeam(TeamRequest teamRequest) {
        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setName(teamRequest.getName());

        return this.teamMapper.toDto(this.teamRepository.save(teamEntity));
    }

    public Team addAthlete(Integer teamId, Integer athleteId) {
        TeamEntity team = getTeamEntityById(teamId);
        UserEntity athlete = this.userService.getUserEntityById(athleteId);

        if(athlete.getRole().getName().equals("SPECTATOR")) {
            throw new UserNotAthleteException("User with id " + athleteId + " is not an athlete");
        }

        if (!team.getAthletes().contains(athlete)) {
            team.getAthletes().add(athlete);
        }

        return this.teamMapper.toDto(this.teamRepository.save(team));
    }

    public Team removeAthlete(Integer teamId, Integer athleteId) {
        TeamEntity team = getTeamEntityById(teamId);
        UserEntity athlete = this.userService.getUserEntityById(athleteId);

        team.getAthletes().remove(athlete);

        return this.teamMapper.toDto(this.teamRepository.save(team));
    }

    public void deleteTeam(Integer teamId) {
        TeamEntity team = getTeamEntityById(teamId);
        this.teamRepository.delete(team);
    }

    private TeamEntity getTeamEntityById(Integer teamId) {
        return this.teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team with id " + teamId + " not found"));
    }
}