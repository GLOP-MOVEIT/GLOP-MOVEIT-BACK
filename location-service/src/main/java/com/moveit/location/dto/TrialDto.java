package com.moveit.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialDto {
    private Integer trialId;
    private Integer competitionId;
    private List<Integer> participantIds;
}