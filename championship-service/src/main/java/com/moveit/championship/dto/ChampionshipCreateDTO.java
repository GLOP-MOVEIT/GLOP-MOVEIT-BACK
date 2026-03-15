package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipCreateDTO {
    @NotBlank(message = "Le nom du championnat est obligatoire")
    private String name;

    @NotBlank(message = "La description du championnat est obligatoire")
    private String description;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    private Status status;
}