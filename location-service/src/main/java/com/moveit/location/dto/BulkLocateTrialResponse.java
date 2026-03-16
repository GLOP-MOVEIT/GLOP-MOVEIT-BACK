package com.moveit.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse de localisation en masse pour une épreuve (trial)")
public class BulkLocateTrialResponse {

    @Schema(description = "Id de l'épreuve (trial)", example = "10")
    private Integer trialId;

    @Schema(description = "Positions des athlètes")
    private List<BulkLocateUserPosition> athletes;

    @Schema(description = "Positions des volontaires")
    private List<BulkLocateUserPosition> volunteers;
}

