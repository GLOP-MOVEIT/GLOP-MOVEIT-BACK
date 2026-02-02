package com.moveit.volunteer_service.dto;

import com.moveit.volunteer_service.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDTO {
    private Long id;
    private Long volunteerId;
    private Long taskId;
    private AssignmentStatus status;
    private String comment;
}
