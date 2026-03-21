package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import com.moveit.notification.entity.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    
    private Long id;
    private String title;
    private String content;
    private NotificationType notificationType;
    private TargetType targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
