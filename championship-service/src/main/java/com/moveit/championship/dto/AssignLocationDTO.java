package com.moveit.championship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignLocationDTO {
    @NotNull(message = "L'ID du lieu est obligatoire")
    private Integer locationId;

    private Integer roundNumber;
}
