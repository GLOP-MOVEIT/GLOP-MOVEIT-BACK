package com.moveit.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocateRequest {
    private Integer requesterId;
    private Integer targetId;
    private Integer trialId;

    public LocateRequest(Integer requesterId, Integer targetId) {
        this.requesterId = requesterId;
        this.targetId = targetId;
        this.trialId = null;
    }
}