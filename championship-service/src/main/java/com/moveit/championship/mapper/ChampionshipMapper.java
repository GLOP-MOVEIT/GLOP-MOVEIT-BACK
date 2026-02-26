package com.moveit.championship.mapper;

import com.moveit.championship.dto.ChampionshipDTO;
import com.moveit.championship.dto.ChampionshipSummaryDTO;
import com.moveit.championship.entity.Championship;

import java.util.Collections;
import java.util.List;

public class ChampionshipMapper {

    private ChampionshipMapper() {
    }

    public static ChampionshipSummaryDTO toChampionshipSummaryDTO(Championship championship) {
        if (championship == null) return null;

        return ChampionshipSummaryDTO.builder()
                .id(championship.getId())
                .name(championship.getName())
                .description(championship.getDescription())
                .startDate(championship.getStartDate())
                .endDate(championship.getEndDate())
                .status(championship.getStatus())
                .build();
    }

    public static List<ChampionshipSummaryDTO> toChampionshipSummaryDTOList(List<Championship> championships) {
        if (championships == null) return Collections.emptyList();
        return championships.stream().map(ChampionshipMapper::toChampionshipSummaryDTO).toList();
    }

    public static ChampionshipDTO toChampionshipDTO(Championship championship) {
        if (championship == null) return null;

        return ChampionshipDTO.builder()
                .id(championship.getId())
                .name(championship.getName())
                .description(championship.getDescription())
                .startDate(championship.getStartDate())
                .endDate(championship.getEndDate())
                .status(championship.getStatus())
                .competitions(championship.getCompetitions() != null
                        ? championship.getCompetitions().stream()
                            .map(CompetitionMapper::toCompetitionSummaryDTO)
                            .toList()
                        : Collections.emptyList())
                .build();
    }
}
