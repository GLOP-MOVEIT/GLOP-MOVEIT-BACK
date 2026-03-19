package com.moveit.championship.service;

import com.moveit.championship.client.UserClient;
import com.moveit.championship.dto.*;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.ParticipantType;
import com.moveit.championship.entity.Trial;
import com.moveit.championship.exception.CompetitionNotFoundException;
import com.moveit.championship.exception.TrialNotFoundException;
import com.moveit.championship.repository.CompetitionRepository;
import com.moveit.championship.repository.TrialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialService {

    private final TrialRepository trialRepository;
    private final CompetitionRepository competitionRepository;
    private final UserClient userClient;


    public Trial getTrialById(Integer id) {
        return trialRepository.findById(id)
                .orElseThrow(() -> new TrialNotFoundException(id));
    }

    public List<Trial> getTrialsByCompetitionId(Integer competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CompetitionNotFoundException(competitionId);
        }
        return trialRepository.findByCompetition_CompetitionId(competitionId);
    }

    @Transactional(readOnly = true)
    public List<Trial> getTrialsByAthleteId(Integer athleteId) {
        List<Integer> participantIds = new ArrayList<>();
        participantIds.add(athleteId);

        try {
            List<TeamResponseDTO> teams = userClient.getTeamsByAthleteId(athleteId);
            participantIds.addAll(teams.stream()
                    .map(TeamResponseDTO::getTeamId)
                    .filter(Objects::nonNull)
                    .toList());
        } catch (Exception e) {
            log.warn("Could not fetch teams for athlete {}: {}", athleteId, e.getMessage());
        }

        List<Integer> distinctParticipantIds = participantIds.stream().distinct().toList();
        return trialRepository.findByParticipantIds(distinctParticipantIds);
    }

    @Transactional
    public Trial createTrial(Integer competitionId, Trial trial) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));

        trial.setCompetition(competition);
        return trialRepository.save(trial);
    }

    @Transactional
    public Trial updateTrial(Integer id, Trial trial) {
        Trial existingTrial = trialRepository.findById(id)
                .orElseThrow(() -> new TrialNotFoundException(id));

        List<Integer> updatedParticipantIds = trial.getParticipantIds() != null
                ? trial.getParticipantIds()
                : existingTrial.getParticipantIds();

        validateScheduleConflict(id, updatedParticipantIds, trial.getTrialStartDate(), trial.getTrialEndDate());

        existingTrial.setTrialName(trial.getTrialName());
        existingTrial.setTrialStartDate(trial.getTrialStartDate());
        existingTrial.setTrialEndDate(trial.getTrialEndDate());
        existingTrial.setTrialDescription(trial.getTrialDescription());
        existingTrial.setTrialStatus(trial.getTrialStatus());
        existingTrial.setLocationId(trial.getLocationId());
        existingTrial.setRoundNumber(trial.getRoundNumber());
        existingTrial.setPosition(trial.getPosition());
        existingTrial.setParticipantIds(updatedParticipantIds);

        return trialRepository.save(existingTrial);
    }

    private void validateScheduleConflict(Integer trialId,
                                          List<Integer> participantIds,
                                          LocalDateTime candidateStart,
                                          LocalDateTime candidateEnd) {
        if (participantIds == null || participantIds.isEmpty()) {
            return;
        }
        if (candidateStart == null || candidateEnd == null) {
            return;
        }

        List<Integer> distinctParticipantIds = participantIds.stream().distinct().toList();
        List<Trial> conflicts = trialRepository.findConflictingTrials(
                trialId, distinctParticipantIds, candidateStart, candidateEnd);

        if (conflicts == null || conflicts.isEmpty()) {
            return;
        }

        Set<Integer> conflictingParticipantIds = new LinkedHashSet<>();
        Set<Integer> conflictTrialIds = new LinkedHashSet<>();

        for (Trial conflictingTrial : conflicts) {
            conflictTrialIds.add(conflictingTrial.getTrialId());
            if (conflictingTrial.getParticipantIds() == null) {
                continue;
            }

            for (Integer participantId : conflictingTrial.getParticipantIds()) {
                if (distinctParticipantIds.contains(participantId)) {
                    conflictingParticipantIds.add(participantId);
                }
            }
        }

        throw new IllegalArgumentException(
                "Conflit d'emploi du temps pour les participants " + conflictingParticipantIds
                        + " avec les épreuves " + conflictTrialIds
                        + " entre " + candidateStart + " et " + candidateEnd);
    }

    @Transactional
    public void deleteTrial(Integer id) {
        if (!trialRepository.existsById(id)) {
            throw new TrialNotFoundException(id);
        }
        trialRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TrialWithParticipantsDTO getTrialWithParticipants(Integer trialId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new TrialNotFoundException(trialId));

        ParticipantType participantType = trial.getCompetition().getParticipantType();
        List<Integer> participantIds = trial.getParticipantIds() == null
                ? Collections.emptyList()
                : trial.getParticipantIds();

        List<ParticipantDTO> participants = participantIds.stream()
                .map(id -> fetchParticipant(id, participantType))
                .toList();

        return TrialWithParticipantsDTO.builder()
                .trialId(trial.getTrialId())
                .trialName(trial.getTrialName())
                .trialStartDate(trial.getTrialStartDate())
                .trialEndDate(trial.getTrialEndDate())
                .trialDescription(trial.getTrialDescription())
                .trialStatus(trial.getTrialStatus())
                .locationId(trial.getLocationId())
                .roundNumber(trial.getRoundNumber())
                .position(trial.getPosition())
                .nextTrialId(trial.getNextTrial() != null ? trial.getNextTrial().getTrialId() : null)
                .competitionId(trial.getCompetition() != null ? trial.getCompetition().getCompetitionId() : null)
                .participants(participants)
                .build();
    }

    private ParticipantDTO fetchParticipant(Integer id, ParticipantType participantType) {
        try {
            return switch (participantType) {
                case TEAM -> {
                    TeamResponseDTO team = userClient.getTeamById(id);
                    yield new ParticipantDTO(team.getTeamId(), team.getName());
                }
                case INDIVIDUAL -> {
                    UserResponseDTO user = userClient.getUserById(id);
                    String fullName = String.format("%s %s",
                            user.getFirstName() == null ? "" : user.getFirstName(),
                            user.getSurname() == null ? "" : user.getSurname()).trim();
                    yield new ParticipantDTO(user.getUserId(), fullName.isBlank() ? "Unknown" : fullName);
                }
            };
        } catch (Exception e) {
            log.warn("Could not fetch participant with id {}: {}", id, e.getMessage());
            return new ParticipantDTO(id, "Unknown");
        }
    }
}
