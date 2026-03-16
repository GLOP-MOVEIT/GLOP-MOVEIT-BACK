package com.moveit.user.mapper;

import com.moveit.user.dto.Team;
import com.moveit.user.entity.TeamEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TeamMapper {

    Team toDto(TeamEntity teamEntity);
}