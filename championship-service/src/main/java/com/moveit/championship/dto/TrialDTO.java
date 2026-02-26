package com.moveit.championship.dto;

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
public class TrialDTO {
    private Integer trialId;
    private String trialName;
    private Date trialStartDate;
    private Date trialEndDate;
    private String trialDescription;
    private Status trialStatus;
    private Integer locationId;
    private Integer roundNumber;
    private Integer position;
    private Integer nextTrialId;
    private Integer competitionId;
    private List<Integer> participantIds;
}
