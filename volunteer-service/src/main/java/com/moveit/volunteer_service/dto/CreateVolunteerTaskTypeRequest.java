package com.moveit.volunteer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolunteerTaskTypeRequest {
    @NotBlank
    private String name;
    private String description;
}
