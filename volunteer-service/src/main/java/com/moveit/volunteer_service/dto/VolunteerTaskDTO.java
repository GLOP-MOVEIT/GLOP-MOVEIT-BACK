package com.moveit.volunteer_service.dto;

import com.moveit.volunteer_service.enums.TaskStatus;
import com.moveit.volunteer_service.enums.TaskTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTaskDTO {
    private Long id;
    private TaskTargetType targetType;
    private Long targetId;
    private String title;
    private String description;
    private Long taskTypeId;
    private TaskStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxVolunteers;
    private String location;
}
