package com.moveit.championship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Integer locationId;
    private String name;
    private Double latitude;
    private Double longitude;
    private String mainEntrance;
    private String refereeEntrance;
    private String athleteEntrance;
    private String description;
}
