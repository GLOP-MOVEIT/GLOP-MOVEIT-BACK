package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO pour créer une notification.
 * Point 5 - Validation des contraintes d'entrée avec min/max pour éviter les données invalides.
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

    private Set<Long> incidentIds = new HashSet<>();

    private Set<Long> eventIds = new HashSet<>();
}
