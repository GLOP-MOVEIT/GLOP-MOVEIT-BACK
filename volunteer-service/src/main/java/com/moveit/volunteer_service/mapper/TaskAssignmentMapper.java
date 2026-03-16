package com.moveit.volunteer_service.mapper;

import com.moveit.volunteer_service.dto.TaskAssignmentDTO;
import com.moveit.volunteer_service.entity.TaskAssignment;

import java.util.List;

public class TaskAssignmentMapper {

    private TaskAssignmentMapper() {}

    public static TaskAssignmentDTO toDTO(TaskAssignment entity) {
        return new TaskAssignmentDTO(
                entity.getId(),
                entity.getVolunteerId(),
                entity.getTask().getId(),
                entity.getStatus(),
                entity.getComment()
        );
    }

    public static List<TaskAssignmentDTO> toDTOList(List<TaskAssignment> entities) {
        return entities.stream().map(TaskAssignmentMapper::toDTO).toList();
    }
}
