package com.moveit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    private Integer requestId;
    private RequestStatus requestStatus;
    private String requestRejectionReason;
    private Role role;
    private String coverLetter;
}