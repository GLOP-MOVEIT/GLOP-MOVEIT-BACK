package com.moveit.volunteer_service.mapper;

import com.moveit.volunteer_service.dto.VolunteerTaskDTO;
import com.moveit.volunteer_service.entity.VolunteerTask;

import java.util.List;

public class VolunteerTaskMapper {

    private VolunteerTaskMapper() {}

    public static VolunteerTaskDTO toDTO(VolunteerTask entity) {
        return new VolunteerTaskDTO(
                entity.getId(),
                entity.getChampionshipId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTaskType().getId(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getMaxVolunteers(),
                entity.getLocation()
        );
    }

    public static List<VolunteerTaskDTO> toDTOList(List<VolunteerTask> entities) {
        return entities.stream().map(VolunteerTaskMapper::toDTO).toList();
    }
}
