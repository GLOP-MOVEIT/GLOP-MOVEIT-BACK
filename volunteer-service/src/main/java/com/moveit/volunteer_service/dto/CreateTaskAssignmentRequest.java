package com.moveit.volunteer_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 1000)
    private String comment;
}
