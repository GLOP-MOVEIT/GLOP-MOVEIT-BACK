package com.moveit.notification.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour mettre à jour une notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateDTO {

    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters if provided")
    private String title;

    @Size(min = 0, max = 5000, message = "Content must not exceed 5000 characters if provided")
    private String content;
}
