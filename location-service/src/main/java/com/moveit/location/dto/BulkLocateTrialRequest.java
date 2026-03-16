package com.moveit.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de localisation en masse pour une épreuve (trial)")
public class BulkLocateTrialRequest {

    @NotNull
    @Schema(description = "Id de l'utilisateur demandeur (referee)", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requesterId;

    @NotNull
    @Schema(description = "Id de l'épreuve (trial)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer trialId;
}

