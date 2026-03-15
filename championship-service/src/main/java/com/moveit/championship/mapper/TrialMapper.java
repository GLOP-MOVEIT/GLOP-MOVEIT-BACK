package com.moveit.championship.mapper;

import com.moveit.championship.dto.TrialDTO;
import com.moveit.championship.dto.TrialRequestDTO;
import com.moveit.championship.entity.Trial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TrialMapper {

    TrialMapper INSTANCE = Mappers.getMapper(TrialMapper.class);

    @Mapping(target = "nextTrialId", expression = "java(trial.getNextTrial() != null ? trial.getNextTrial().getTrialId() : null)")
    @Mapping(target = "competitionId", expression = "java(trial.getCompetition() != null ? trial.getCompetition().getCompetitionId() : null)")
    TrialDTO toTrialDTO(Trial trial);

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
}