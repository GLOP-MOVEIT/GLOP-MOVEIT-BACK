package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;

import java.time.LocalDateTime;

public class CompetitionUpdateDTOMother {
    public static CompetitionUpdateDTO validUpdate() {
        CompetitionUpdateDTO dto = new CompetitionUpdateDTO();
        dto.setCompetitionName("Compétition mise à jour");
        dto.setCompetitionStartDate(LocalDateTime.now());
        dto.setCompetitionEndDate(LocalDateTime.now().plusDays(1));
        dto.setCompetitionDescription("Description mise à jour");
        dto.setCompetitionStatus(Status.ONGOING);
        return dto;
    }
}
