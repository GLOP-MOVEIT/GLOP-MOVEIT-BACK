package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer une nouvelle notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Level name is required")
    private String levelName; // CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL

    @NotBlank(message = "Notification title is required")
    private String name;

    private String body;

    private Integer securityId;

    private Integer competitionId;
}
