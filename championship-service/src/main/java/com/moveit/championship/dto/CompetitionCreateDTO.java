package com.moveit.championship.dto;

import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.ParticipantType;
import com.moveit.championship.entity.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompetitionCreateDTO {
    @NotBlank(message = "Le sport de la competition est obligatoire")
    private String competitionSport;

    @NotBlank(message = "Le nom de la competition est obligatoire")
    private String competitionName;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDateTime competitionStartDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime competitionEndDate;

    private String competitionDescription;

    private String competitionResultUnit;

    private Status competitionStatus;

    @NotNull(message = "Le nombre de manches est obligatoire")
    private Integer nbManches;

    private CompetitionType competitionType;

    private Integer maxPerHeat;

    private ParticipantType participantType;

    private Integer assignedCommissaireId;

    @Valid
    @NotNull(message = "Le championship est obligatoire")
    private ChampionshipRefDTO championship;

    @Data
    public static class ChampionshipRefDTO {
        @NotNull(message = "Championship ID is required")
        private Integer id;
    }
}