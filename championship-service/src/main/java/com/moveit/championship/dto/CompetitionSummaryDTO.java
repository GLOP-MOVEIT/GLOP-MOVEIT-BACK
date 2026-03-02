package com.moveit.championship.dto;

import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.ParticipantType;
import com.moveit.championship.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionSummaryDTO {
    private Integer competitionId;
    private Integer championshipId;
    private String competitionSport;
    private String competitionName;
    private Date competitionStartDate;
    private Date competitionEndDate;
    private String competitionDescription;
    private Status competitionStatus;
    private Integer nbManches;
    private CompetitionType competitionType;
    private Integer maxPerHeat;
    private ParticipantType participantType;
}
