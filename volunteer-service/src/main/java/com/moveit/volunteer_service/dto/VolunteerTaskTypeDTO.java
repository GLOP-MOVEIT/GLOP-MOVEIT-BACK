package com.moveit.volunteer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTaskTypeDTO {
    private Long id;
    private String name;
    private String description;
}
