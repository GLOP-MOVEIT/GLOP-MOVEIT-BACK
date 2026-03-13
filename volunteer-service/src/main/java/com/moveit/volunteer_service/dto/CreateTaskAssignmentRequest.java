package com.moveit.volunteer_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskAssignmentRequest {
    @NotNull
    private Long volunteerId;
    @NotNull
    private Long taskId;
}
