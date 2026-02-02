package com.moveit.volunteer_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolunteerPreferenceRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long taskTypeId;
    @NotNull
    private Integer preferenceOrder;
    @Size(max = 255)
    private String notes;
}
