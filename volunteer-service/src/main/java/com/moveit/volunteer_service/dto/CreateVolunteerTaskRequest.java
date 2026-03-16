package com.moveit.volunteer_service.dto;

import com.moveit.volunteer_service.enums.TaskTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolunteerTaskRequest {
    @NotNull
    private TaskTargetType targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long taskTypeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @NotNull
    private Integer maxVolunteers;
    @NotNull
    private Long locationId;
    @Size(max = 500)
    private String location;
}
