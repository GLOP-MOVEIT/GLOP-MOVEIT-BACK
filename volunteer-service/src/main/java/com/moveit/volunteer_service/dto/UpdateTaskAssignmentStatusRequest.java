package com.moveit.volunteer_service.dto;

import com.moveit.volunteer_service.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskAssignmentStatusRequest {
    @NotNull
    private AssignmentStatus status;
    @Size(max = 1000)
    private String comment;
}
