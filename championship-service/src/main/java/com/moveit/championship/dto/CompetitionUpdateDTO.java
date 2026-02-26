package com.moveit.championship.dto;

import com.moveit.championship.entity.Status;
import lombok.Data;
import java.util.Date;

@Data
public class CompetitionUpdateDTO {
    private String competitionName;
    private Date competitionStartDate;
    private Date competitionEndDate;
    private String competitionDescription;
    private Status competitionStatus;
}
