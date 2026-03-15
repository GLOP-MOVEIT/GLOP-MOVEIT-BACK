package com.moveit.championship.mapper;

import com.moveit.championship.dto.CompetitionCreateDTO;
import com.moveit.championship.dto.CompetitionDTO;
import com.moveit.championship.dto.CompetitionSummaryDTO;
import com.moveit.championship.dto.EventDTO;
import com.moveit.championship.dto.TrialDTO;
import com.moveit.championship.dto.TrialRequestDTO;
import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Event;
import com.moveit.championship.entity.Trial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetitionMapper {

    @Mapping(target = "nextTrialId", expression = "java(trial.getNextTrial() != null ? trial.getNextTrial().getTrialId() : null)")
    @Mapping(target = "competitionId", expression = "java(trial.getCompetition() != null ? trial.getCompetition().getCompetitionId() : null)")
    TrialDTO toTrialDTO(Trial trial);

    EventDTO toEventDTO(Event event);

    @Mapping(target = "championshipId", expression = "java(competition.getChampionship() != null ? competition.getChampionship().getId() : null)")
    @Mapping(target = "events", expression = "java(toEventDTOList(competition.getEvents()))")
    @Mapping(target = "trials", expression = "java(toSortedTrialDTOList(competition.getTrials()))")
    CompetitionDTO toCompetitionDTO(Competition competition);

    @Mapping(target = "championshipId", expression = "java(competition.getChampionship() != null ? competition.getChampionship().getId() : null)")
    CompetitionSummaryDTO toCompetitionSummaryDTO(Competition competition);

    default List<CompetitionDTO> toCompetitionDTOList(List<Competition> competitions) {
        if (competitions == null) {
            return Collections.emptyList();
        }
        return competitions.stream().map(this::toCompetitionDTO).toList();
    }

    default List<CompetitionSummaryDTO> toCompetitionSummaryDTOList(List<Competition> competitions) {
        if (competitions == null) {
            return Collections.emptyList();
        }
        return competitions.stream().map(this::toCompetitionSummaryDTO).toList();
    }

    default Competition toCompetitionEntity(CompetitionCreateDTO dto) {
        Championship championship = new Championship();
        championship.setId(dto.getChampionship().getId());

        Competition competition = new Competition();
        competition.setCompetitionSport(dto.getCompetitionSport());
        competition.setCompetitionName(dto.getCompetitionName());
        competition.setCompetitionStartDate(dto.getCompetitionStartDate());
        competition.setCompetitionEndDate(dto.getCompetitionEndDate());
        competition.setCompetitionDescription(dto.getCompetitionDescription());
        competition.setCompetitionStatus(dto.getCompetitionStatus());
        competition.setNbManches(dto.getNbManches());
        competition.setCompetitionType(dto.getCompetitionType());
        competition.setMaxPerHeat(dto.getMaxPerHeat());
        competition.setParticipantType(dto.getParticipantType());
        competition.setAssignedCommissaireId(dto.getAssignedCommissaireId());
        competition.setChampionship(championship);
        return competition;
    }

    default Trial toTrialEntity(TrialRequestDTO dto) {
        Trial trial = new Trial();
        trial.setTrialName(dto.getTrialName());
        trial.setTrialStartDate(dto.getTrialStartDate());
        trial.setTrialEndDate(dto.getTrialEndDate());
        trial.setTrialDescription(dto.getTrialDescription());
        trial.setTrialStatus(dto.getTrialStatus());
        trial.setLocationId(dto.getLocationId());
        trial.setRoundNumber(dto.getRoundNumber());
        trial.setPosition(dto.getPosition());
        trial.setParticipantIds(dto.getParticipantIds());
        return trial;
    }

    default List<TrialDTO> toSortedTrialDTOList(List<Trial> trials) {
        if (trials == null) {
            return Collections.emptyList();
        }
        return trials.stream()
                .sorted(Comparator.comparing(Trial::getRoundNumber).thenComparing(Trial::getPosition))
                .map(this::toTrialDTO)
                .toList();
    }

    default List<EventDTO> toEventDTOList(List<Event> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        return events.stream().map(this::toEventDTO).toList();
    }
}
