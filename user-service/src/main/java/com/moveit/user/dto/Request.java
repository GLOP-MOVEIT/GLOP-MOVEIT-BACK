package com.moveit.user.dto;

import com.moveit.user.entity.DocumentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    private Integer requestId;
    private RequestStatus requestStatus;
    private String requestRejectionReason;
    private Role role;
    private List<DocumentEntity> documents;
}