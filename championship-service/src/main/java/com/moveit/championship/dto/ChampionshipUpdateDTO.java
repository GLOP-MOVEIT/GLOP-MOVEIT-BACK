package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipUpdateDTO {
    @NotBlank(message = "Le nom du championnat est obligatoire")
    private String name;

    @NotBlank(message = "La description du championnat est obligatoire")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private Date startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private Date endDate;

    private Status status;
}
