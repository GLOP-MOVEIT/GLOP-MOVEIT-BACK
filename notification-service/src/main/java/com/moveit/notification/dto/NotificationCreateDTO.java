package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer une notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {

    @NotBlank(message = "Title is required and cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(min = 0, max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Target type is required")
    private TargetType targetType = TargetType.GLOBAL;

    private Long targetId;
}
