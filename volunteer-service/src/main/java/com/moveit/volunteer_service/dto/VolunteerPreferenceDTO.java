package com.moveit.volunteer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerPreferenceDTO {
    private Long id;
    private Long userId;
    private Long taskTypeId;
    private Integer preferenceOrder;
    private String notes;
}
