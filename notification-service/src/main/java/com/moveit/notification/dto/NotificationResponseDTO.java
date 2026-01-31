package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    
    private Long id;
    private String title;
    private String content;
    private NotificationType notificationType;
    private Set<Long> incidentIds;
    private Set<Long> eventIds;
    private LocalDateTime createdAt;
}
