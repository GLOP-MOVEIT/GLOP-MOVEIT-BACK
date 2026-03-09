package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
}
