package com.moveit.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private Integer resultId;
    @NotNull
    private Integer trialId;
    @NotNull
    private boolean lastTrial;
    @NotNull
    private List<Ranking> rankings;
}