package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialRequestDTO {
    @NotBlank(message = "Le nom de la manche est obligatoire")
    private String trialName;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDateTime trialStartDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime trialEndDate;

    private String trialDescription;

    private Status trialStatus;

    private Integer locationId;

    private Integer roundNumber;

    private Integer position;

    private List<Integer> participantIds;
}