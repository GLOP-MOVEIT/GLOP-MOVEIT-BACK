package com.moveit.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Position localisée d'un utilisateur")
public class BulkLocateUserPosition {

    @Schema(example = "12")
    private Integer userId;

    @Schema(example = "Camille")
    private String firstName;

    @Schema(example = "Barthelemy")
    private String surname;

    @Schema(example = "48.8566")
    private double latitude;

    @Schema(example = "2.3522")
    private double longitude;
}
