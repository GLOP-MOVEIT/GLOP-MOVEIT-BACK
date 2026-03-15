package com.moveit.championship.mapper;

import com.moveit.championship.dto.ChampionshipDTO;
import com.moveit.championship.dto.ChampionshipCreateDTO;
import com.moveit.championship.dto.ChampionshipSummaryDTO;
import com.moveit.championship.entity.Championship;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = CompetitionMapper.class)
public interface ChampionshipMapper {

    ChampionshipSummaryDTO toChampionshipSummaryDTO(Championship championship);

    ChampionshipDTO toChampionshipDTO(Championship championship);

    Championship toChampionshipEntity(ChampionshipCreateDTO dto);

    default List<ChampionshipSummaryDTO> toChampionshipSummaryDTOList(List<Championship> championships) {
        if (championships == null) {
            return Collections.emptyList();
        }
        return championships.stream().map(this::toChampionshipSummaryDTO).toList();
    }
}
