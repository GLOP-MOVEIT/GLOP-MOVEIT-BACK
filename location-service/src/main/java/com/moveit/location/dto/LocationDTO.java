package com.moveit.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
    private Integer locationId;

    @NotBlank(message = "Le nom du lieu est obligatoire")
    private String name;

    @NotNull(message = "La latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    private Double longitude;

    @NotBlank(message = "L'entree principale est obligatoire")
    private String mainEntrance;

    private String refereeEntrance;

    private String athleteEntrance;

    private String description;
}