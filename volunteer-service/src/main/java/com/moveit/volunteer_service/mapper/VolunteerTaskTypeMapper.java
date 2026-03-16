package com.moveit.volunteer_service.mapper;

import com.moveit.volunteer_service.dto.VolunteerTaskTypeDTO;
import com.moveit.volunteer_service.entity.VolunteerTaskType;

import java.util.List;

public class VolunteerTaskTypeMapper {

    private VolunteerTaskTypeMapper() {}

    public static VolunteerTaskTypeDTO toDTO(VolunteerTaskType entity) {
        return new VolunteerTaskTypeDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    public static List<VolunteerTaskTypeDTO> toDTOList(List<VolunteerTaskType> entities) {
        return entities.stream().map(VolunteerTaskTypeMapper::toDTO).toList();
    }
}
