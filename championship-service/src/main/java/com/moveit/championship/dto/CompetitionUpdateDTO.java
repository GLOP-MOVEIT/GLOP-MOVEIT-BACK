package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompetitionUpdateDTO {
    private String competitionName;
    private LocalDateTime competitionStartDate;
    private LocalDateTime competitionEndDate;
    private String competitionDescription;
    private Status competitionStatus;
}
