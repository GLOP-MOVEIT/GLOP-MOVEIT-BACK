package com.moveit.championship.mapper;

import com.moveit.championship.dto.CompetitionDTO;
import com.moveit.championship.dto.EventDTO;
import com.moveit.championship.dto.TrialDTO;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Event;
import com.moveit.championship.entity.Trial;

import java.util.Collections;
import java.util.List;

public class CompetitionMapper {

    private CompetitionMapper() {
    }

    public static TrialDTO toTrialDTO(Trial trial) {
        if (trial == null) return null;

        return TrialDTO.builder()
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
                .participantIds(trial.getParticipantIds())
                .build();
    }

    public static EventDTO toEventDTO(Event event) {
        if (event == null) return null;

        return EventDTO.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .eventDate(event.getEventDate())
                .eventDescription(event.getEventDescription())
                .build();
    }

    public static CompetitionDTO toCompetitionDTO(Competition competition) {
        if (competition == null) return null;

        List<TrialDTO> trialDTOs = competition.getTrials() != null
                ? competition.getTrials().stream()
                    .sorted(java.util.Comparator.comparing(com.moveit.championship.entity.Trial::getRoundNumber)
                        .thenComparing(com.moveit.championship.entity.Trial::getPosition))
                    .map(CompetitionMapper::toTrialDTO).toList()
                : Collections.emptyList();

        List<EventDTO> eventDTOs = competition.getEvents() != null
                ? competition.getEvents().stream().map(CompetitionMapper::toEventDTO).toList()
                : Collections.emptyList();

        return CompetitionDTO.builder()
                .competitionId(competition.getCompetitionId())
                .championshipId(competition.getChampionship() != null ? competition.getChampionship().getId() : null)
                .competitionSport(competition.getCompetitionSport())
                .competitionName(competition.getCompetitionName())
                .competitionStartDate(competition.getCompetitionStartDate())
                .competitionEndDate(competition.getCompetitionEndDate())
                .competitionDescription(competition.getCompetitionDescription())
                .competitionStatus(competition.getCompetitionStatus())
                .events(eventDTOs)
                .trials(trialDTOs)
                .nbManches(competition.getNbManches())
                .competitionType(competition.getCompetitionType())
                .maxPerHeat(competition.getMaxPerHeat())
                .participantType(competition.getParticipantType())
                .build();
    }

    public static List<CompetitionDTO> toCompetitionDTOList(List<Competition> competitions) {
        if (competitions == null) return Collections.emptyList();
        return competitions.stream().map(CompetitionMapper::toCompetitionDTO).toList();
    }
}
