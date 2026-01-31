package com.moveit.notification.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO pour mettre à jour une notification.
 * Point 5 - Validation des contraintes d'entrée avec min/max pour éviter les données invalides.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateDTO {

    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters if provided")
    private String title;

    @Size(min = 0, max = 5000, message = "Content must not exceed 5000 characters if provided")
    private String content;

    private Set<Long> incidentIds;

    private Set<Long> eventIds;
}
