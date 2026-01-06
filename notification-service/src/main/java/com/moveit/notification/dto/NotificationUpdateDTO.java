package com.moveit.notification.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateDTO {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    private Set<Long> incidentIds;

    private Set<Long> eventIds;
}
