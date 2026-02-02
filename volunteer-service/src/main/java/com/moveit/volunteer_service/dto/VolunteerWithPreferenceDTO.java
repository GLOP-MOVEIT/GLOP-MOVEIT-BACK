package com.moveit.volunteer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerWithPreferenceDTO {
    private Long volunteerId;
    private Boolean hasPreference;
    private Integer preferenceOrder;
    private String preferenceComment;
}
