package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;

import java.util.Date;

public class CompetitionUpdateDTOMother {
    public static CompetitionUpdateDTO validUpdate() {
        CompetitionUpdateDTO dto = new CompetitionUpdateDTO();
        dto.setCompetitionName("Compétition mise à jour");
        dto.setCompetitionStartDate(new Date());
        dto.setCompetitionEndDate(new Date(System.currentTimeMillis() + 86400000));
        dto.setCompetitionDescription("Description mise à jour");
        dto.setCompetitionStatus(Status.ONGOING);
        return dto;
    }
}
