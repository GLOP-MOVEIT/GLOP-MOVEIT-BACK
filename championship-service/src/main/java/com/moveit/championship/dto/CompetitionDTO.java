package com.moveit.championship.dto;

import com.moveit.championship.entity.CompetitionType;
import com.moveit.championship.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionDTO {
    private Integer competitionId;
    private Integer championshipId;
    private String competitionSport;
    private String competitionName;
    private Date competitionStartDate;
    private Date competitionEndDate;
    private String competitionDescription;
    private Status competitionStatus;
    private List<EventDTO> events;
    private List<TrialDTO> trials;
    private Integer nbManches;
    private CompetitionType competitionType;
    private Integer maxPerHeat;
}
