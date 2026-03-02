package com.moveit.volunteer_service.mapper;

import com.moveit.volunteer_service.dto.VolunteerPreferenceDTO;
import com.moveit.volunteer_service.entity.VolunteerPreference;

import java.util.List;

public class VolunteerPreferenceMapper {

    private VolunteerPreferenceMapper() {}

    public static VolunteerPreferenceDTO toDTO(VolunteerPreference entity) {
        return new VolunteerPreferenceDTO(
                entity.getId(),
                entity.getUserId(),
                entity.getTaskType().getId(),
                entity.getPreferenceOrder(),
                null
        );
    }

    public static List<VolunteerPreferenceDTO> toDTOList(List<VolunteerPreference> entities) {
        return entities.stream().map(VolunteerPreferenceMapper::toDTO).toList();
    }
}
