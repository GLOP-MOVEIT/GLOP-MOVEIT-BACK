package com.moveit.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour marquer une notification comme lue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAsReadRequest {

    @NotNull(message = "Read status is required")
    private Boolean read;
}
