package com.moveit.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequest {

    @NotBlank(message = "Rejection reason is required")
    private String requestRejectionReason;
}