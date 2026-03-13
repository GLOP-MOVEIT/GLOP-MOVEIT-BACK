package com.moveit.volunteer_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckVolunteerPreferencesRequest {
    @NotNull
    private Long taskTypeId;

    @NotEmpty
    private List<Long> volunteerIds;
}